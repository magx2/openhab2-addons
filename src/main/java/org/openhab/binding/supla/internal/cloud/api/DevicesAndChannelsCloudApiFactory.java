package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

final class DevicesAndChannelsCloudApiFactory implements ChannelsCloudApiFactory, IoDevicesCloudApiFactory {
    static final DevicesAndChannelsCloudApiFactory FACTORY = new DevicesAndChannelsCloudApiFactory();
    private final ApiClientFactory apiClientFactory;
    private final ConcurrentMap<String, DevicesAndChannelsCloudApi> instances = new ConcurrentHashMap<>();

    DevicesAndChannelsCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private DevicesAndChannelsCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public ChannelsCloudApi newChannelsCloudApi(final String token,
                                                final long cacheEvictionTime,
                                                final TimeUnit timeUnit) {
        return instances.computeIfAbsent(token, t -> newInstance(t, cacheEvictionTime, timeUnit));
    }

    @Override
    public IoDevicesCloudApi newIoDevicesCloudApi(final String token,
                                                  final long cacheEvictionTime,
                                                  final TimeUnit timeUnit) {
        return instances.computeIfAbsent(token, t -> newInstance(t, cacheEvictionTime, timeUnit));
    }

    private DevicesAndChannelsCloudApi newInstance(final String token,
                                                   final long cacheEvictionTime,
                                                   final TimeUnit timeUnit) {
        final Api api = apiClientFactory.newApiClient(token);
        return new DevicesAndChannelsCloudApi(
                api.getChannelApi(),
                api.getDeviceApi(),
                cacheEvictionTime,
                timeUnit);
    }
}
