package org.openhab.binding.supla.internal.cloud.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerIoDevicesCloudApiFactory implements IoDevicesCloudApiFactory {
    public static final SwaggerIoDevicesCloudApiFactory FACTORY = new SwaggerIoDevicesCloudApiFactory();
    private static final Logger swaggerIoDevicesCloudApiLogger = LoggerFactory.getLogger(SwaggerChannelsCloudApi.class);
    private final ApiClientFactory apiClientFactory;

    public SwaggerIoDevicesCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private SwaggerIoDevicesCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public IoDevicesCloudApi newIoDevicesCloudApi(final String token) {
        return new SwaggerIoDevicesCloudApi(apiClientFactory.newApiClient(token, swaggerIoDevicesCloudApiLogger));
    }
}
