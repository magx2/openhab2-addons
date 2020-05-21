package org.openhab.binding.supla.internal.cloud.executors;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.Color;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.action.Action;
import pl.grzeslowski.jsupla.api.channel.action.SetBrightnessAction;
import pl.grzeslowski.jsupla.api.channel.action.SetBrightnessAndColor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.math.MathContext.UNLIMITED;
import static java.util.Optional.ofNullable;

@SuppressWarnings("PackageAccessibility")
final class SuplaLedCommandExecutor implements LedCommandExecutor {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private final Logger logger = LoggerFactory.getLogger(SuplaLedCommandExecutor.class);
    private final Map<Integer, LedState> ledStates = new HashMap<>();
    private final ChannelsCloudApi channelsApi;

    SuplaLedCommandExecutor(final ChannelsCloudApi channelsApi) {
        this.channelsApi = channelsApi;
    }

    @Override
    public void setLedState(final Channel channel, PercentType brightness) {
        final Optional<LedState> ledState = findLedState(channel);
        if (ledState.isPresent()) {
            ledStates.put(channel.getId(), new LedState(ledState.get().hsb, brightness));
        } else {
            ledStates.put(channel.getId(), new LedState(null, brightness));
        }
    }

    @Override
    public void setLedState(final Channel channel, HSBType hsb) {
        final Optional<LedState> ledState = findLedState(channel);
        if (ledState.isPresent()) {
            ledStates.put(channel.getId(), new LedState(hsb, ledState.get().brightness));
        } else {
            ledStates.put(channel.getId(), new LedState(hsb, null));
        }
    }

    @Override
    public void changeColor(final Channel channel, final HSBType command) {
        final Optional<LedState> state = findLedState(channel);
        state.ifPresent(ledState -> sendNewLedValue(channel, command, ledState.brightness));
    }

    @Override
    public void changeColorBrightness(final Channel channel, final PercentType command) {
        final Optional<LedState> state = findLedState(channel);
        if (state.isPresent()) {
            final LedState ledState = state.get();
            final HSBType newHsbType = new HSBType(
                    ledState.hsb.getHue(),
                    ledState.hsb.getSaturation(),
                    command
            );
            sendNewLedValue(channel, newHsbType, ledState.brightness);
        }
    }

    @Override
    public void changeBrightness(final Channel channel, final PercentType command) {
        final Optional<LedState> state = findLedState(channel);
        state.ifPresent(ledState -> sendNewLedValue(channel, ledState.hsb, command));
    }

    private void sendNewLedValue(
            final Channel channel,
            final HSBType hsbType,
            final PercentType brightness) {
        final Action action;
        if (hsbType != null) {
            final int colorBrightness = hsbType.getBrightness().intValue();
            final Color.Hsv hsv = new Color.Hsv(
                    hsbType.getHue().doubleValue(),
                    hsbType.getSaturation().toBigDecimal().divide(ONE_HUNDRED, UNLIMITED).doubleValue(),
                    1.0);
            logger.trace("Changing HSV to `{}` (command `{}`), color brightness {}%", hsv, hsbType, colorBrightness);
            action = new SetBrightnessAndColor(colorBrightness, hsv);
        } else if (brightness != null) {
            logger.trace("Changing brightness {}%", brightness);
            action = new SetBrightnessAction(brightness.intValue());
        } else {
            throw new IllegalStateException("Cannot `sendNewLedValue` for channel with ID `" + channel.getId() + "`");
        }

        channelsApi.executeAction(channel, action);
        ledStates.put(channel.getId(), new LedState(hsbType, brightness));
    }

    private Optional<LedState> findLedState(final Channel channel) {
        final Optional<LedState> ledState = ofNullable(ledStates.get(channel.getId()));
        if (!ledState.isPresent()) {
            logger.warn("There is no LED state for channel `{}`!", channel.getId());
        }
        return ledState;
    }

    private static class LedState {
        private final HSBType hsb;
        private final PercentType brightness;

        private LedState(final HSBType hsb, final PercentType brightness) {
            this.hsb = hsb;
            this.brightness = brightness;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final LedState ledState = (LedState) o;
            return hsb.equals(ledState.hsb) &&
                           Objects.equals(brightness, ledState.brightness);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hsb, brightness);
        }

        @Override
        public String toString() {
            return "LedState{" +
                           "hsb=" + hsb +
                           ", brightness=" + brightness +
                           '}';
        }
    }
}
