package org.openhab.binding.supla.internal.cloud.api;

import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface ChannelsCloudApiFactory {
    static ChannelsCloudApiFactory getFactory() {
        return DevicesAndChannelsCloudApiFactory.FACTORY;
    }

    ChannelsCloudApi newChannelsCloudApi(String token,
                                         long cacheEvictionTime,
                                         TimeUnit timeUnit);
}
