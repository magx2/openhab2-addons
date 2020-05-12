package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.generated.ApiClient;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.api.ServerApi;
import pl.grzeslowski.jsupla.api.generated.model.ServerInfo;

final class SwaggerServerCloudApi implements ServerCloudApi {
    private final ServerApi serverApi;

    SwaggerServerCloudApi(final ApiClient apiClient) {
        serverApi = new ServerApi(apiClient);
    }

    @Override
    public ServerInfo getServerInfo() throws ApiException {
        return serverApi.getServerInfo();
    }
}
