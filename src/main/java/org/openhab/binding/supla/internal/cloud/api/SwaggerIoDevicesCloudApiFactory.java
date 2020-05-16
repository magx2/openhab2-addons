package org.openhab.binding.supla.internal.cloud.api;


final class SwaggerIoDevicesCloudApiFactory implements IoDevicesCloudApiFactory {
    static final SwaggerIoDevicesCloudApiFactory FACTORY = new SwaggerIoDevicesCloudApiFactory();
    private final ApiClientFactory apiClientFactory;

    @SuppressWarnings("WeakerAccess")
    SwaggerIoDevicesCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private SwaggerIoDevicesCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public IoDevicesCloudApi newIoDevicesCloudApi(final String token) {
        return new SwaggerIoDevicesCloudApi(apiClientFactory.newApiClient(token));
    }
}
