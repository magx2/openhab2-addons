package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.Api;

public interface ApiClientFactory {
    Api newApiClient(String token);
}
