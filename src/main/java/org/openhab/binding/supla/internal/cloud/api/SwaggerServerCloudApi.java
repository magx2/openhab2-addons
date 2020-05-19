package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;
import pl.grzeslowski.jsupla.api.ServerInfoApi;
import pl.grzeslowski.jsupla.api.serverinfo.ServerInfo;

final class SwaggerServerCloudApi implements ServerCloudApi {
    private final ServerInfoApi serverInfoApi;
    private final String apiVersion;

    SwaggerServerCloudApi(final Api api) {
        serverInfoApi = api.getServerInfoApi();
        apiVersion = api.getApiVersion();
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfoApi.findServerInfo();
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }
}
