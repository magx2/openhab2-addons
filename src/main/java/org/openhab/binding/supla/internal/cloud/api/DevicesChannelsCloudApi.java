package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.ChannelApi;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.action.Action;
import pl.grzeslowski.jsupla.api.device.Device;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

final class DevicesChannelsCloudApi implements ChannelsCloudApi {
    private final ChannelApi channelApi;
    private final IoDevicesCloudApi ioDevicesCloudApi;

    DevicesChannelsCloudApi(final ChannelApi channelApi, final IoDevicesCloudApi ioDevicesCloudApi) {
        this.channelApi = requireNonNull(channelApi, "channelApi");
        this.ioDevicesCloudApi = requireNonNull(ioDevicesCloudApi, "ioDevicesCloudApi");
    }

    @Override
    public void executeAction(final Channel channel, final Action action) {
        channelApi.updateState(channel, action);
    }

    @Override
    public Channel getChannel(final int id) {
        return ioDevicesCloudApi.getIoDevices()
                       .stream()
                       .map(Device::getChannels)
                       .flatMap(Collection::stream)
                       .filter(channel -> channel.getId() == id)
                       .findAny()
                       .orElseThrow(() -> new IllegalArgumentException("There is no channel with ID=" + id));
    }
}
