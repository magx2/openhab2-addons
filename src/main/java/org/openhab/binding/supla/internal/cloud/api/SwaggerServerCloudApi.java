package org.openhab.binding.supla.internal.cloud.api;

import org.openhab.binding.supla.internal.UpdateWhenNeededMonad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.ServerInfoApi;
import pl.grzeslowski.jsupla.api.serverinfo.ServerInfo;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class SwaggerServerCloudApi implements ServerCloudApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerServerCloudApi.class);
    private final String apiVersion;
    private final UpdateWhenNeededMonad<ServerInfo> serverInfoMonad;

    SwaggerServerCloudApi(final ServerInfoApi serverInfoApi,
                          final String apiVersion,
                          final long cacheEvictionTime,
                          final TimeUnit timeUnit) {
        serverInfoMonad = new UpdateWhenNeededMonad<>(findServerInfoApiInCloud(serverInfoApi), cacheEvictionTime, timeUnit);
        this.apiVersion = requireNonNull(apiVersion, "apiVersion");
    }

    private Supplier<ServerInfo> findServerInfoApiInCloud(final ServerInfoApi serverInfoApi) {
        requireNonNull(serverInfoApi, "serverInfoApi");
        return () -> {
            LOGGER.trace("Missed cache for `serverInfoApi`");
            return serverInfoApi.findServerInfo();
        };
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfoMonad.get();
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }
}
