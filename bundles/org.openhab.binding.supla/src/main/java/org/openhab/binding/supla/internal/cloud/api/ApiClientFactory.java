package org.openhab.binding.supla.internal.cloud.api;

import org.slf4j.Logger;
import pl.grzeslowski.jsupla.api.generated.ApiClient;

public interface ApiClientFactory {
    ApiClient newApiClient(String token, Logger logger);
}
