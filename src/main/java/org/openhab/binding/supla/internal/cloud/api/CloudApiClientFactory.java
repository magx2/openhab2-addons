package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class CloudApiClientFactory implements ApiClientFactory {
    static final CloudApiClientFactory FACTORY = new CloudApiClientFactory();
    private final ConcurrentMap<String, Api> apiCache = new ConcurrentHashMap<>();

    /**
     * Use {@link CloudApiClientFactory#FACTORY}
     */
    private CloudApiClientFactory() {
    }

    public Api newApiClient(String token) {
        return apiCache.computeIfAbsent(token, Api::getInstance);
    }
}
