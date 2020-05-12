package org.openhab.binding.supla.internal.cloud.api;

public interface ServerCloudApiFactory {
    static ServerCloudApiFactory getFactory() {
        return SwaggerServerCloudApiFactory.FACTORY;
    }

    ServerCloudApi newServerCloudApi(String token);
}
