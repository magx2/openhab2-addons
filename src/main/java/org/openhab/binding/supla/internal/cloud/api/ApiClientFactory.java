package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;

public interface ApiClientFactory {
    static ApiClientFactory getInstance() {
        return CloudApiClientFactory.FACTORY;
    }

    Api newApiClient(String token);
}
