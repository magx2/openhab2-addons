package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.generated.ApiClient;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.api.ChannelsApi;
import pl.grzeslowski.jsupla.api.generated.model.Channel;
import pl.grzeslowski.jsupla.api.generated.model.ChannelExecuteActionRequest;

import java.util.List;

final class SwaggerChannelsCloudApi implements ChannelsCloudApi {
    private final ChannelsApi channelsApi;

    SwaggerChannelsCloudApi(final ApiClient apiClient) {
        channelsApi = new ChannelsApi(apiClient);
    }

    @Override
    public void executeAction(final ChannelExecuteActionRequest body, final Integer id) throws ApiException {
        channelsApi.executeAction(body, id);
    }

    @Override
    public Channel getChannel(final int id, final List<String> include) throws ApiException {
        return channelsApi.getChannel(id, include);
    }
}
