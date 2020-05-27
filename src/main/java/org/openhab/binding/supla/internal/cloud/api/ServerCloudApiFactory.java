package org.openhab.binding.supla.internal.cloud.api;

import java.util.concurrent.TimeUnit;

public interface ServerCloudApiFactory {
    static ServerCloudApiFactory getFactory() {
        return SwaggerServerCloudApiFactory.FACTORY;
    }

    ServerCloudApi newServerCloudApi(String token, long cacheEvictionTime, TimeUnit timeUnit);
}
