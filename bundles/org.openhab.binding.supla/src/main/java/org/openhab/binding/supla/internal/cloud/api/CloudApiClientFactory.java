package org.openhab.binding.supla.internal.cloud.api;

import org.slf4j.Logger;
import pl.grzeslowski.jsupla.api.generated.ApiClient;

import static com.squareup.okhttp.logging.HttpLoggingInterceptor.Level.BODY;

final class CloudApiClientFactory implements ApiClientFactory {
    static final CloudApiClientFactory FACTORY = new CloudApiClientFactory();

    public ApiClient newApiClient(String token, Logger logger) {
        final ApiClient apiClient = pl.grzeslowski.jsupla.api.ApiClientFactory.INSTANCE.newApiClient(token);
        if (logger != null) {
            apiClient.setDebugging(logger.isDebugEnabled(), new OneLineHttpLoggingInterceptor(logger::trace, BODY));
        }
        return apiClient;
    }
}
