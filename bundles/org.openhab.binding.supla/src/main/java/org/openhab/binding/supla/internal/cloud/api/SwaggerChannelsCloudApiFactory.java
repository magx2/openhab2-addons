package org.openhab.binding.supla.internal.cloud.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SwaggerChannelsCloudApiFactory implements ChannelsCloudApiFactory {
    static final SwaggerChannelsCloudApiFactory FACTORY = new SwaggerChannelsCloudApiFactory();
    private static final Logger swaggerChannelsCloudApiLogger = LoggerFactory.getLogger(SwaggerChannelsCloudApi.class);
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
        return new SwaggerChannelsCloudApi(apiClientFactory.newApiClient(token, swaggerChannelsCloudApiLogger));
    }
}
