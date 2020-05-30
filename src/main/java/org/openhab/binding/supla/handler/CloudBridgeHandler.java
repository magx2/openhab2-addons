package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.supla.internal.ReadWriteMonad;
import org.openhab.binding.supla.internal.cloud.api.ApiClientFactory;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApiFactory;
import org.openhab.binding.supla.internal.cloud.api.IoDevicesCloudApiFactory;
import org.openhab.binding.supla.internal.cloud.api.ServerCloudApi;
import org.openhab.binding.supla.internal.cloud.api.ServerCloudApiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.Api;
import pl.grzeslowski.jsupla.api.serverinfo.ServerInfo;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.supla.SuplaBindingConstants.ADDRESS_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.API_LAST_UPDATE_DATE_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.API_LIMIT_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.API_REMAINING_LIMIT_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.API_RESET_DATE_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.API_VERSION_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.CLOUD_VERSION_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.CLEAR_CACHES_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.REFRESH_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.O_AUTH_TOKEN;
import static org.openhab.binding.supla.SuplaBindingConstants.REFRESH_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.THREAD_POOL_NAME;

public class CloudBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(CloudBridgeHandler.class);
    private final ReadWriteMonad<Set<CloudDeviceHandler>> cloudDeviceHandlers = new ReadWriteMonad<>(new HashSet<>());
    private final ServerCloudApiFactory serverCloudApiFactory;
    private String oAuthToken;
    private String address;
    private String apiVersion;
    private String cloudVersion;
    private ScheduledFuture<?> scheduledFuture;
    private Long refreshInterval;
    private Api api;

    CloudBridgeHandler(final Bridge bridge, final ServerCloudApiFactory serverCloudApiFactory) {
        super(bridge);
        this.serverCloudApiFactory = serverCloudApiFactory;
    }

    public CloudBridgeHandler(final Bridge bridge) {
        this(bridge, ServerCloudApiFactory.getFactory());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (Exception ex) {
            logger.error("Cannot start server!", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Cannot start server! " + ex.getMessage());
        }
    }

    private void internalInitialize() {
        // init bridge api client
        final Configuration config = this.getConfig();
        this.oAuthToken = (String) config.get(O_AUTH_TOKEN);
        this.refreshInterval = ((BigDecimal) config.get("refreshInterval")).longValue();

        // get server info
        ServerCloudApi serverApi;
        try {
            serverApi = serverCloudApiFactory.newServerCloudApi(oAuthToken, refreshInterval, SECONDS);
        } catch (Exception e) {
            logger.error("Cannot create client to Supla Cloud! Probably oAuth token is incorrect!", e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Cannot create client to Supla Cloud! Probably oAuth token is incorrect! " + e.getMessage());
            return;
        }
        ServerInfo serverInfo = serverApi.getServerInfo();
        if (serverInfo == null) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Cannot get server info from server!");
            return;
        }
        address = serverInfo.getAddress();
        apiVersion = serverInfo.getApiVersion();
        cloudVersion = serverInfo.getCloudVersion();
        updateState(ADDRESS_CHANNEL_ID, new StringType(address));
        updateState(API_VERSION_CHANNEL_ID, new StringType(apiVersion));
        updateState(CLOUD_VERSION_CHANNEL_ID, new StringType(cloudVersion));

        api = ApiClientFactory.getInstance().newApiClient(oAuthToken);
        updateApiUsageStatisticsChannels();

        // check if current api is supported
        String apiVersion = serverApi.getApiVersion();
        List<String> supportedApiVersions = serverInfo.getSupportedVersions();
        if (!supportedApiVersions.contains(apiVersion)) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "This API version `" + apiVersion
                                                               + "` is not supported! Supported api versions: [" + String.join(", ", supportedApiVersions) + "].");
            return;
        }

        final ScheduledExecutorService scheduledPool = ThreadPoolManager.getScheduledPool(THREAD_POOL_NAME);
        this.scheduledFuture = scheduledPool.scheduleAtFixedRate(
                this::refreshCloudDevices,
                refreshInterval * 2,
                refreshInterval,
                SECONDS);

        // done
        updateStatus(ONLINE);
    }

    private void updateApiUsageStatisticsChannels() {
        final Optional<Api.ApiUsageStatistics> apiUsageStatistics = api.getApiUsageStatistics();
        final long limit;
        final long remainingLimit;
        final ZonedDateTime resetDate;
        final ZonedDateTime lastUpdateDate;
        if (apiUsageStatistics.isPresent()) {
            logger.trace("Updating api usage statistics for {}#{}", CloudBridgeHandler.class.getSimpleName(), thing.getUID());
            final Api.ApiUsageStatistics statistics = apiUsageStatistics.get();
            limit = statistics.getLimit();
            remainingLimit = statistics.getRemainingLimit();
            resetDate = statistics.getResetDate();
            lastUpdateDate = statistics.getLastUpdateDate();
        } else {
            logger.trace("No api usage statistics for {}#{}", CloudBridgeHandler.class.getSimpleName(), thing.getUID());
            limit = 0;
            remainingLimit = 0;
            resetDate = ZonedDateTime.now();
            lastUpdateDate = ZonedDateTime.now();
        }
        updateState(API_LIMIT_ID, new DecimalType(limit));
        updateState(API_REMAINING_LIMIT_ID, new DecimalType(remainingLimit));
        updateState(API_RESET_DATE_ID, new DateTimeType(computeZonedDateTimeForCurrentSystem(resetDate)));
        updateState(API_LAST_UPDATE_DATE_ID, new DateTimeType(computeZonedDateTimeForCurrentSystem(lastUpdateDate)));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        final String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            logger.trace("Refreshing channel `{}` in {}", channelUID, CloudBridgeHandler.class.getSimpleName());
            if (ADDRESS_CHANNEL_ID.equals(channelId)) {
                updateState(ADDRESS_CHANNEL_ID, new StringType(address));
            } else if (API_VERSION_CHANNEL_ID.equals(channelId)) {
                updateState(API_VERSION_CHANNEL_ID, new StringType(apiVersion));
            } else if (CLOUD_VERSION_CHANNEL_ID.equals(channelId)) {
                updateState(CLOUD_VERSION_CHANNEL_ID, new StringType(cloudVersion));
            } else if (API_LIMIT_ID.equals(channelId)
                               || API_REMAINING_LIMIT_ID.equals(channelId)
                               || API_RESET_DATE_ID.equals(channelId)
                               || API_LAST_UPDATE_DATE_ID.equals(channelId)) {
                updateApiUsageStatisticsChannels();
            }
        } else if (command instanceof StringType) {
            if (command.toFullString().equals(REFRESH_COMMAND)) {
                if (REFRESH_CHANNEL_ID.equals(channelId)) {
                    logger.debug("Refreshing all devices in {}", CloudBridgeHandler.class.getSimpleName());
                    clearCaches();
                    ThreadPoolManager.getPool(THREAD_POOL_NAME).submit(this::refreshCloudDevices);
                }
            } else if (command.toFullString().equals(CLEAR_CACHES_COMMAND)) {
                if (REFRESH_CHANNEL_ID.equals(channelId)) {
                    logger.debug("Clearing caches in {}", CloudBridgeHandler.class.getSimpleName());
                    clearCaches();
                }
            }
        }
    }

    private void clearCaches() {
        ChannelsCloudApiFactory.getFactory().clearCaches(oAuthToken);
        IoDevicesCloudApiFactory.getFactory().clearCaches(oAuthToken);
    }

    private ZonedDateTime computeZonedDateTimeForCurrentSystem(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(ZoneId.systemDefault());
    }

    public Optional<String> getOAuthToken() {
        return ofNullable(oAuthToken);
    }

    public Optional<Long> getRefreshInterval() {
        return ofNullable(refreshInterval);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof CloudDeviceHandler) {
            logger.trace("Add `{}` to cloudDeviceHandlers", childHandler.getThing().getUID());
            cloudDeviceHandlers.doInWriteLock(
                    cloudDeviceHandlers -> cloudDeviceHandlers.add((CloudDeviceHandler) childHandler));
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        if (childHandler instanceof CloudDeviceHandler) {
            logger.trace("Remove `{}` to cloudDeviceHandlers", childHandler.getThing().getUID());
            cloudDeviceHandlers.doInWriteLock(cloudDeviceHandlers -> cloudDeviceHandlers.remove(childHandler));
        }
    }

    private void refreshCloudDevices() {
        logger.info("Starting to refresh cloud devices");
        cloudDeviceHandlers.doInReadLock(
                cloudDeviceHandlers -> cloudDeviceHandlers.forEach(CloudDeviceHandler::refresh));
        this.refresh();
    }

    private void refresh() {
        thing.getChannels()
                .stream()
                .map(Channel::getUID)
                .forEach(id -> handleCommand(id, REFRESH));
    }
}
