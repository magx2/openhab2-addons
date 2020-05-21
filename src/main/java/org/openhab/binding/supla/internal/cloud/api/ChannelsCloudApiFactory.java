package org.openhab.binding.supla.internal.cloud.api;

@FunctionalInterface
public interface ChannelsCloudApiFactory {
    static ChannelsCloudApiFactory getFactory() {
        return DevicesChannelsCloudApiFactory.FACTORY;
    }

    ChannelsCloudApi newChannelsCloudApi(String token);
}
