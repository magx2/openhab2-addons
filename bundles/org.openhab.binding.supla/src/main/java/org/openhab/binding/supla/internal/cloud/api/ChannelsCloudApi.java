package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.Channel;
import pl.grzeslowski.jsupla.api.generated.model.ChannelExecuteActionRequest;

import java.util.List;

public interface ChannelsCloudApi {
    void executeAction(ChannelExecuteActionRequest body, Integer id) throws ApiException;

    Channel getChannel(int id, List<String> include) throws ApiException;
}
