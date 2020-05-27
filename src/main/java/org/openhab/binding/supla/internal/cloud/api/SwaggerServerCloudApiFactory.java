package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;

import java.util.concurrent.TimeUnit;

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
    public ServerCloudApi newServerCloudApi(final String token,
                                            final long cacheEvictionTime,
                                            final TimeUnit timeUnit) {
        final Api api = apiClientFactory.newApiClient(token);
        return new SwaggerServerCloudApi(api.getServerInfoApi(), api.getApiVersion(), cacheEvictionTime, timeUnit);
    }
}
