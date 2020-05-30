package org.openhab.binding.supla.internal.cloud.api;

import org.openhab.binding.supla.internal.UpdateWhenNeededMonad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.ChannelApi;
import pl.grzeslowski.jsupla.api.DeviceApi;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.action.Action;
import pl.grzeslowski.jsupla.api.device.Device;

import java.util.Collection;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class DevicesAndChannelsCloudApi implements ChannelsCloudApi, IoDevicesCloudApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevicesAndChannelsCloudApi.class);
    private final ChannelApi channelApi;
    private final UpdateWhenNeededMonad<SortedSet<Device>> devicesMonad;

    DevicesAndChannelsCloudApi(final ChannelApi channelApi,
                               final DeviceApi ioDevicesApi,
                               final long cacheEvictionTime,
                               final TimeUnit timeUnit) {
        this.channelApi = requireNonNull(channelApi, "channelApi");
        devicesMonad = new UpdateWhenNeededMonad<>(findDevicesInCloud(ioDevicesApi), cacheEvictionTime, timeUnit);
    }

    private Supplier<SortedSet<Device>> findDevicesInCloud(final DeviceApi ioDevicesApi) {
        requireNonNull(ioDevicesApi, "ioDevicesApi");
        return () -> {
            LOGGER.trace("Missed cache for `devices`");
            return ioDevicesApi.findDevices();
        };
    }

    @Override
    public void executeAction(final Channel channel, final Action action) {
        channelApi.updateState(channel, action);
    }

    @Override
    public Channel getChannel(final int id) {
        return devicesMonad.get()
                       .stream()
                       .map(Device::getChannels)
                       .flatMap(Collection::stream)
                       .filter(channel -> channel.getId() == id)
                       .findAny()
                       .orElseThrow(() -> new IllegalArgumentException("There is no channel with ID=" + id));
    }

    @Override
    public Device getIoDevice(final int id) {
        return devicesMonad.get()
                       .stream()
                       .filter(device -> device.getId() == id)
                       .findAny()
                       .orElseThrow(() -> new IllegalArgumentException("There is no IO Device with ID=" + id));
    }

    @Override
    public SortedSet<Device> getIoDevices() {
        return devicesMonad.get();
    }

    public void clearCaches() {
        devicesMonad.clearCaches();
    }
}
