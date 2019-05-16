package org.openhab.binding.supla.internal.cloud.executors;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import pl.grzeslowski.jsupla.api.generated.ApiException;

public interface LedCommandExecutor {
    void setLedState(ChannelUID channelUID, PercentType brightness);

    void setLedState(ChannelUID channelUID, HSBType hsb);

    void changeColor(final int channelId, final ChannelUID channelUID, final HSBType command) throws ApiException;

    void changeColorBrightness(final int channelId, final ChannelUID channelUID, final PercentType command) throws ApiException;

    void changeBrightness(final int channelId, final ChannelUID channelUID, final PercentType command) throws ApiException;
}
