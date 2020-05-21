package org.openhab.binding.supla.internal.cloud.api;

final class DevicesChannelsCloudApiFactory implements ChannelsCloudApiFactory {
    static final DevicesChannelsCloudApiFactory FACTORY = new DevicesChannelsCloudApiFactory();
    private final ApiClientFactory apiClientFactory;

    DevicesChannelsCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private DevicesChannelsCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public ChannelsCloudApi newChannelsCloudApi(final String token) {
        return new DevicesChannelsCloudApi(
                apiClientFactory.newApiClient(token).getChannelApi(),
                IoDevicesCloudApiFactory.getFactory().newIoDevicesCloudApi(token));
    }
}
