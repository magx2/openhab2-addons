package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;

final class CloudApiClientFactory implements ApiClientFactory {
    static final CloudApiClientFactory FACTORY = new CloudApiClientFactory();

    public Api newApiClient(String token) {
        return Api.getInstance(token);
    }
}
