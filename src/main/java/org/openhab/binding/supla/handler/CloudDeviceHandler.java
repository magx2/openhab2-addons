package org.openhab.binding.supla.handler;

import io.swagger.client.model.ChannelFunctionActionEnum;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.supla.internal.cloud.ChannelInfo;
import org.openhab.binding.supla.internal.cloud.ChannelInfoParser;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApiFactory;
import org.openhab.binding.supla.internal.cloud.api.IoDevicesCloudApi;
import org.openhab.binding.supla.internal.cloud.api.IoDevicesCloudApiFactory;
import org.openhab.binding.supla.internal.cloud.executors.LedCommandExecutor;
import org.openhab.binding.supla.internal.cloud.executors.LedCommandExecutorFactory;
import org.openhab.binding.supla.internal.cloud.executors.SuplaLedCommandExecutorFactory;
import org.openhab.binding.supla.internal.cloud.functionswitch.CreateChannelFunctionSwitch;
import org.openhab.binding.supla.internal.cloud.functionswitch.FindStateFunctionSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.channel.DimmerAndRgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.DimmerChannel;
import pl.grzeslowski.jsupla.api.channel.GateChannel;
import pl.grzeslowski.jsupla.api.channel.RgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.RollerShutterChannel;
import pl.grzeslowski.jsupla.api.channel.action.Action;
import pl.grzeslowski.jsupla.api.channel.action.OpenCloseAction;
import pl.grzeslowski.jsupla.api.channel.action.ShutRevealAction;
import pl.grzeslowski.jsupla.api.channel.action.StopAction;
import pl.grzeslowski.jsupla.api.channel.action.ToggleAction;
import pl.grzeslowski.jsupla.api.channel.action.TurnOnOffAction;
import pl.grzeslowski.jsupla.api.device.Device;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.eclipse.smarthome.core.library.types.UpDownType.UP;
import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.BRIDGE_UNINITIALIZED;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.NONE;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.OFF_LIGHT_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.OPEN_CLOSE_GATE_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.WHITE_LIGHT_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_DEVICE_CLOUD_ID;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.EXTRA_LIGHT_ACTIONS;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.LED_BRIGHTNESS;
import static pl.grzeslowski.jsupla.api.channel.ChannelDispatcher.DISPATCHER;

/**
 * This is handler for all Supla devices.
 * <p>
 * Channels are created at runtime after connecting to Supla Cloud
 *
 * @author Martin Grześlowski - initial contributor
 */
@SuppressWarnings("PackageAccessibility")
public final class CloudDeviceHandler extends AbstractDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(CloudBridgeHandler.class);
    private final ChannelsCloudApiFactory channelsCloudApiFactory;
    private final IoDevicesCloudApiFactory ioDevicesCloudApiFactory;
    private final LedCommandExecutorFactory ledCommandExecutorFactory;

    private ChannelsCloudApi channelsApi;
    private int cloudId;
    private IoDevicesCloudApi ioDevicesApi;

    // CommandExecutors
    private LedCommandExecutor ledCommandExecutor;

    CloudDeviceHandler(
            final Thing thing,
            final ChannelsCloudApiFactory channelsCloudApiFactory,
            final IoDevicesCloudApiFactory ioDevicesCloudApiFactory,
            final LedCommandExecutorFactory ledCommandExecutorFactory) {
        super(thing);
        this.channelsCloudApiFactory = channelsCloudApiFactory;
        this.ioDevicesCloudApiFactory = ioDevicesCloudApiFactory;
        this.ledCommandExecutorFactory = ledCommandExecutorFactory;
    }

    public CloudDeviceHandler(final Thing thing) {
        this(
                thing,
                ChannelsCloudApiFactory.getFactory(),
                IoDevicesCloudApiFactory.getFactory(),
                SuplaLedCommandExecutorFactory.FACTORY);
    }

    @Override
    protected void internalInitialize() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("No bridge for thing with UID {}", thing.getUID());
            updateStatus(OFFLINE, BRIDGE_UNINITIALIZED,
                    "There is no bridge for this thing. Remove it and add it again.");
            return;
        }
        final BridgeHandler bridgeHandler = bridge.getHandler();
        if (!(bridgeHandler instanceof CloudBridgeHandler)) {
            logger.debug("Bridge is not instance of {}! Current bridge class {}, Thing UID {}",
                    CloudBridgeHandler.class.getSimpleName(), bridgeHandler.getClass().getSimpleName(), thing.getUID());
            updateStatus(OFFLINE, BRIDGE_UNINITIALIZED, "There is wrong type of bridge for cloud device!");
            return;
        }
        CloudBridgeHandler handler = (CloudBridgeHandler) bridgeHandler;
        final Optional<String> token = handler.getOAuthToken();
        if (!token.isPresent()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "There is no OAuth token in bridge!");
            return;
        }
        initApi(token.get());

        if (!initCloudApi()) {
            return;
        }

        if (!checkIfIsOnline()) {
            return;
        }

        if (!checkIfIsEnabled()) {
            return;
        }

        initChannels();
        initCommandExecutors();

        // done
        updateStatus(ONLINE);
    }

    private boolean initCloudApi() {
        final String cloudIdString = valueOf(getConfig().get(SUPLA_DEVICE_CLOUD_ID));
        try {
            this.cloudId = parseInt(cloudIdString);
            return true;
        } catch (NumberFormatException e) {
            logger.error("Cannot parse cloud ID `{}` to integer!", cloudIdString, e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "Cloud ID is incorrect!");
            return false;
        }
    }

    private void initApi(final String token) {
        ioDevicesApi = ioDevicesCloudApiFactory.newIoDevicesCloudApi(token);
        channelsApi = channelsCloudApiFactory.newChannelsCloudApi(token);
    }

    private boolean checkIfIsOnline() {
        final Device device = ioDevicesApi.getIoDevice(cloudId);
        if (!device.isConnected()) {
            updateStatus(OFFLINE, NONE, "This device is is not connected to Supla Cloud.");
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIfIsEnabled() {
        final Device device = ioDevicesApi.getIoDevice(cloudId);
        if (!device.isEnabled()) {
            updateStatus(OFFLINE, NONE, "This device is turned off in Supla Cloud.");
            return false;
        } else {
            return true;
        }
    }

    private void initChannels() {
        try {
            final List<Channel> channels = ioDevicesApi.getIoDevice(cloudId)
                                                   .getChannels()
                                                   .stream()
                                                   .filter(channel -> !channel.isHidden())
                                                   .map(channel -> DISPATCHER.dispatch(channel, new CreateChannelFunctionSwitch(thing.getUID())))
                                                   .flatMap(List::stream)
                                                   .collect(Collectors.toList());
            updateChannels(channels);
        } catch (Exception e) {
            logger.error("Error when loading IO device from Supla Cloud!", e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, "Error when loading IO device from Supla Cloud!");
        }
    }

    private void initCommandExecutors() {
        ledCommandExecutor = ledCommandExecutorFactory.newLedCommandExecutor(channelsApi);
    }

    private void updateChannels(final List<Channel> channels) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    @Override
    protected void handleRefreshCommand(final ChannelUID channelUID) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        logger.trace("Refreshing channel `{}`", channelUID);
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);
        final FindStateFunctionSwitch findStateFunctionSwitch = new FindStateFunctionSwitch(ledCommandExecutor, channelUID);
        Optional<? extends State> foundState = DISPATCHER.dispatch(channel, findStateFunctionSwitch);
        if (foundState.isPresent()) {
            final State state = foundState.get();
            logger.trace("Updating state `{}` to `{}`", channelUID, state);
            updateState(channelUID, state);
        } else {
            logger.warn("There was no found state for channel `{}` channelState={}, function={}",
                    channelUID, channel.findState(), channel.getClass().getSimpleName());
        }
    }

    @Override
    protected void handleOnOffCommand(final ChannelUID channelUID, final OnOffType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);
        if (channel instanceof GateChannel) {
            handleOneZeroCommand(channel, command == ON, OpenCloseAction.OPEN, OpenCloseAction.CLOSE);
        } else {
            handleOneZeroCommand(channel, command == ON, TurnOnOffAction.ON, TurnOnOffAction.OFF);
        }
    }

    @Override
    protected void handleUpDownCommand(final ChannelUID channelUID, final UpDownType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);

        if (channel instanceof RollerShutterChannel) {
            handleOneZeroCommand(channel, command == UP, ShutRevealAction.reveal(), ShutRevealAction.shut());
        } else {
            logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
        }
    }

    @Override
    protected void handleHsbCommand(final ChannelUID channelUID, final HSBType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);
        handleHsbCommand(channel, channelUID, command);
    }

    private void handleHsbCommand(final pl.grzeslowski.jsupla.api.channel.Channel channel,
                                  final ChannelUID channelUID,
                                  final HSBType command) {
        if (channel instanceof RgbLightningChannel || channel instanceof DimmerAndRgbLightningChannel) {
            ledCommandExecutor.changeColor(channel, command);
        } else {
            logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
        }
    }

    @Override
    protected void handleOpenClosedCommand(final ChannelUID channelUID, final OpenClosedType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);

        if (channel instanceof GateChannel) {
            handleOneZeroCommand(channel,
                    command == OpenClosedType.OPEN,
                    OpenCloseAction.OPEN,
                    OpenCloseAction.CLOSE);
        } else {
            logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
        }
    }

    @Override
    protected void handlePercentCommand(final ChannelUID channelUID, final PercentType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);
        if (channel instanceof RollerShutterChannel) {
            final int shut = 100 - command.intValue();
            logger.debug("Channel `{}` is roller shutter; setting shut={}%", channelUID, shut);
            final Action action = ShutRevealAction.shut(shut);
            channelsApi.executeAction(channel, action);
        } else if (channel instanceof RgbLightningChannel || channel instanceof DimmerAndRgbLightningChannel) {
            if (channelInfo.getAdditionalChannelType() == null) {
                ledCommandExecutor.changeColorBrightness(channel, command);
            } else if (channelInfo.getAdditionalChannelType() == LED_BRIGHTNESS) {
                ledCommandExecutor.changeBrightness(channel, command);
            }
        } else if (channel instanceof DimmerChannel) {
            ledCommandExecutor.changeBrightness(channel, command);
        } else {
            logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
        }
    }

    @Override
    protected void handleDecimalCommand(final ChannelUID channelUID, final DecimalType command) {
        // TODO handle this command
        logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
    }

    private void handleOneZeroCommand(final pl.grzeslowski.jsupla.api.channel.Channel channel,
                                      final boolean firstOrSecond,
                                      final Action first,
                                      final Action second) {
        final Action action = firstOrSecond ? first : second;
        logger.trace("Executing 0/1 command `{}`", action);
        channelsApi.executeAction(channel, action);
    }

    @Override
    protected void handleStopMoveTypeCommand(final @NotNull ChannelUID channelUID, final @NotNull StopMoveType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);
        if (channel instanceof RollerShutterChannel) {
            handleStopMoveTypeCommandOnRollerShutter(channelUID, channel, command);
        } else {
            logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
        }
    }

    private void handleStopMoveTypeCommandOnRollerShutter(
            final ChannelUID channelUID,
            final pl.grzeslowski.jsupla.api.channel.Channel channel,
            final StopMoveType command) {
        switch (command) {
            case MOVE:
                logger.trace("Do not know how to handle command `{}` on roller shutter with id `{}`", command, channelUID);
                return;
            case STOP:
                final ChannelFunctionActionEnum action = ChannelFunctionActionEnum.STOP;
                logger.trace("Sending stop action `{}` to channel with UUID `{}`", action, channelUID);
                channelsApi.executeAction(channel, StopAction.STOP);
        }
    }

    @Override
    protected void handleStringCommand(final ChannelUID channelUID, final StringType command) {
        final ChannelInfo channelInfo = ChannelInfoParser.PARSER.parse(channelUID);
        final int channelId = channelInfo.getChannelId();
        final pl.grzeslowski.jsupla.api.channel.Channel channel = queryForChannel(channelId);
        if (channel instanceof GateChannel) {
            if (command.toFullString().equals(OPEN_CLOSE_GATE_COMMAND)) {
                channelsApi.executeAction(channel, ToggleAction.OPEN_CLOSE);
            }
        } else if (channel instanceof RgbLightningChannel || channel instanceof DimmerAndRgbLightningChannel) {
            if (EXTRA_LIGHT_ACTIONS.equals(channelInfo.getAdditionalChannelType())) {
                final ChannelUID mainLightChannel = new ChannelUID(channelUID.getThingUID(), String.valueOf(channelId));
                if (command.toFullString().equals(WHITE_LIGHT_COMMAND)) {
                    changeColorOfRgb(channel, HSBType.WHITE, mainLightChannel);
                } else if (command.toFullString().equals(OFF_LIGHT_COMMAND)) {
                    changeColorOfRgb(channel, HSBType.BLACK, mainLightChannel);
                }
            }
        } else {
            logger.warn("Not handling `{}` on channel `{}#{}`", command, command.getClass().getSimpleName(), channelUID);
        }
    }

    private void changeColorOfRgb(final pl.grzeslowski.jsupla.api.channel.Channel channel,
                                  final HSBType hsbType,
                                  final ChannelUID rgbChannelUid) {
        logger.trace("Setting color to `{}` for channel `{}`", hsbType, rgbChannelUid);
        handleHsbCommand(channel, rgbChannelUid, hsbType);
        updateState(rgbChannelUid, hsbType);
    }

    void refresh() {
        logger.debug("Refreshing `{}`", thing.getUID());
        try {
            if (checkIfIsOnline() && checkIfIsEnabled()) {
                updateStatus(ONLINE);
                logger.trace("Thing `{}` is connected & enabled. Refreshing channels", thing.getUID());
                thing.getChannels()
                        .stream()
                        .map(Channel::getUID)
                        .forEach(channelUID -> handleCommand(channelUID, REFRESH));
            }
        } catch (Exception e) {
            logger.error("Cannot check if device `{}` is online/enabled", thing.getUID(), e);
        }
    }

    private pl.grzeslowski.jsupla.api.channel.Channel queryForChannel(final int channelId) {
        return channelsApi.getChannel(channelId);
    }

}
