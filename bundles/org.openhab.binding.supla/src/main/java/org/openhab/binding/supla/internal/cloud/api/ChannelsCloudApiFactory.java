package org.openhab.binding.supla.internal.cloud.api;

@FunctionalInterface
public interface ChannelsCloudApiFactory {
    ChannelsCloudApi newChannelsCloudApi(String token);
}
