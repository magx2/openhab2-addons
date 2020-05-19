package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.serverinfo.ServerInfo;

public interface ServerCloudApi {
    ServerInfo getServerInfo();

    String getApiVersion();
}
