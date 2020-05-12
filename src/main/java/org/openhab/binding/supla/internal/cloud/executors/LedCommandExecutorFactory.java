package org.openhab.binding.supla.internal.cloud.executors;

import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;

public interface LedCommandExecutorFactory {
    LedCommandExecutor newLedCommandExecutor(ChannelsCloudApi channelsCloudApi);
}
