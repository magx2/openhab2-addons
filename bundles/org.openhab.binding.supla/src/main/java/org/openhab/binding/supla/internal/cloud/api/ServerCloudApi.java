package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.ServerInfo;

public interface ServerCloudApi {
    ServerInfo getServerInfo() throws ApiException;
}
