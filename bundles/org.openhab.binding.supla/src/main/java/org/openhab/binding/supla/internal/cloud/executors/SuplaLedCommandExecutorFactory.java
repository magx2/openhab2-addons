package org.openhab.binding.supla.internal.cloud.executors;

import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;

public final class SuplaLedCommandExecutorFactory implements LedCommandExecutorFactory {
    public static final SuplaLedCommandExecutorFactory FACTORY = new SuplaLedCommandExecutorFactory();

    @Override
    public LedCommandExecutor newLedCommandExecutor(final ChannelsCloudApi channelsCloudApi) {
        return new SuplaLedCommandExecutor(channelsCloudApi);
    }
}
