package org.openhab.binding.supla.internal.cloud.api;

final class SwaggerChannelsCloudApiFactory implements ChannelsCloudApiFactory {
    static final SwaggerChannelsCloudApiFactory FACTORY = new SwaggerChannelsCloudApiFactory();
    private final ApiClientFactory apiClientFactory;

    @SuppressWarnings("WeakerAccess")
    SwaggerChannelsCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private SwaggerChannelsCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public ChannelsCloudApi newChannelsCloudApi(String token) {
        return new SwaggerChannelsCloudApi(apiClientFactory.newApiClient(token));
    }
}
