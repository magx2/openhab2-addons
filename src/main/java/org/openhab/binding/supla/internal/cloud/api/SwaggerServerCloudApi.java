package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;
import pl.grzeslowski.jsupla.api.ServerInfoApi;
import pl.grzeslowski.jsupla.api.serverinfo.ServerInfo;

final class SwaggerServerCloudApi implements ServerCloudApi {
    private final ServerInfoApi serverInfoApi;

    SwaggerServerCloudApi(final Api api) {
        serverInfoApi = api.getServerInfoApi();
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfoApi.findServerInfo();
    }
}
