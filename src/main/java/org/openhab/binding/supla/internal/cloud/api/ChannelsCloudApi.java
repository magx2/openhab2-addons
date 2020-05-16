package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.action.Action;

public interface ChannelsCloudApi {
    void executeAction(final Channel channel, final Action action);

    Channel getChannel(int id);
}
