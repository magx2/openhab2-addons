package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;
import pl.grzeslowski.jsupla.api.ChannelApi;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.action.Action;

final class SwaggerChannelsCloudApi implements ChannelsCloudApi {
    private final ChannelApi channelsApi;

    SwaggerChannelsCloudApi(final Api api) {
        channelsApi = api.getChannelApi();
    }

    @Override
    public void executeAction(final Channel channel, final Action action) {
        channelsApi.updateState(channel, action);
    }

    @Override
    public Channel getChannel(final int id) {
        return channelsApi.findChannel(id);
    }
}
