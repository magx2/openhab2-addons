package org.openhab.binding.supla.internal.cloud.api;

import java.util.concurrent.TimeUnit;

public interface IoDevicesCloudApiFactory {
    static IoDevicesCloudApiFactory getFactory() {
        return DevicesAndChannelsCloudApiFactory.FACTORY;
    }

    IoDevicesCloudApi newIoDevicesCloudApi(String token, long cacheEvictionTime, TimeUnit timeUnit);

    void clearCaches(final String oAuthToken);
}
