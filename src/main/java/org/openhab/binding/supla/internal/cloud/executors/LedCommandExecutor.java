package org.openhab.binding.supla.internal.cloud.executors;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import pl.grzeslowski.jsupla.api.channel.Channel;

public interface LedCommandExecutor {
    void setLedState(final Channel channel, final PercentType brightness);

    void setLedState(final Channel channel, final HSBType hsb);

    void changeColor(final Channel channel, final HSBType command);

    void changeColorBrightness(final Channel channel, final PercentType command);

    void changeBrightness(final Channel channel, final PercentType command);
}
