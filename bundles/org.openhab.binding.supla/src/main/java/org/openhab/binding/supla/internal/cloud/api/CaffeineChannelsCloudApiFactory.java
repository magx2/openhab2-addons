package org.openhab.binding.supla.internal.cloud.api;

final class CaffeineChannelsCloudApiFactory implements ChannelsCloudApiFactory {
    private final ChannelsCloudApiFactory channelsCloudApiFactory;

    CaffeineChannelsCloudApiFactory(final ChannelsCloudApiFactory channelsCloudApiFactory) {
        this.channelsCloudApiFactory = channelsCloudApiFactory;
    }

    @Override
    public ChannelsCloudApi newChannelsCloudApi(final String token) {
        return new CaffeineChannelsCloudApi(channelsCloudApiFactory.newChannelsCloudApi(token));
    }
}
