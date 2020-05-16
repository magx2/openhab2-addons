package org.openhab.binding.supla.internal.cloud.api;

final class SwaggerServerCloudApiFactory implements ServerCloudApiFactory {
    static final SwaggerServerCloudApiFactory FACTORY = new SwaggerServerCloudApiFactory();
    private final ApiClientFactory apiClientFactory;

    @SuppressWarnings("WeakerAccess")
    SwaggerServerCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private SwaggerServerCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public ServerCloudApi newServerCloudApi(final String token) {
        return new SwaggerServerCloudApi(apiClientFactory.newApiClient(token));
    }
}
