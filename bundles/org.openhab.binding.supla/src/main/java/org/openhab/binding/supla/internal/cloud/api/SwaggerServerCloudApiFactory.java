package org.openhab.binding.supla.internal.cloud.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerServerCloudApiFactory implements ServerCloudApiFactory {
    public static final SwaggerServerCloudApiFactory FACTORY = new SwaggerServerCloudApiFactory();
    private static final Logger swaggerServerCloudApiLogger = LoggerFactory.getLogger(SwaggerServerCloudApi.class);
    private final ApiClientFactory apiClientFactory;

    public SwaggerServerCloudApiFactory(final ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    private SwaggerServerCloudApiFactory() {
        this(CloudApiClientFactory.FACTORY);
    }

    @Override
    public ServerCloudApi newServerCloudApi(final String token) {
        return new SwaggerServerCloudApi(apiClientFactory.newApiClient(token, swaggerServerCloudApiLogger));
    }
}
