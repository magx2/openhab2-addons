package org.openhab.binding.supla.internal.cloud.api;

public interface ServerCloudApiFactory {
    ServerCloudApi newServerCloudApi(String token);
}
