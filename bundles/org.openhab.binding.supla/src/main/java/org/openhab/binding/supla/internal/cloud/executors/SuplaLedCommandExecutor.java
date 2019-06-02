package org.openhab.binding.supla.internal.cloud.executors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.supla.internal.cloud.HsbTypeConverter;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.ChannelExecuteActionRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.SET_RGBW_PARAMETERS;

@SuppressWarnings("PackageAccessibility")
final class SuplaLedCommandExecutor implements LedCommandExecutor {
    private final Logger logger = LoggerFactory.getLogger(SuplaLedCommandExecutor.class);
    private final Map<ChannelUID, LedState> ledStates = new HashMap<>();
    private final ChannelsCloudApi channelsApi;

    SuplaLedCommandExecutor(final ChannelsCloudApi channelsApi) {
        this.channelsApi = channelsApi;
    }

    @Override
    public void setLedState(ChannelUID channelUID, PercentType brightness) {
        final Optional<LedState> ledState = findLedState(channelUID);
        if (ledState.isPresent()) {
            ledStates.put(channelUID, new LedState(ledState.get().hsb, brightness));
        } else {
            ledStates.put(channelUID, new LedState(null, brightness));
        }
    }

    @Override
    public void setLedState(ChannelUID channelUID, HSBType hsb) {
        final Optional<LedState> ledState = findLedState(channelUID);
        if (ledState.isPresent()) {
            ledStates.put(channelUID, new LedState(hsb, ledState.get().brightness));
        } else {
            ledStates.put(channelUID, new LedState(hsb, null));
        }
    }

    @Override
    public void changeColor(final int channelId, final ChannelUID channelUID, final HSBType command) throws ApiException {
        final Optional<LedState> state = findLedState(channelUID);
        if (state.isPresent()) {
            sendNewLedValue(channelUID, channelId, command, state.get().brightness);
        }
    }

    @Override
    public void changeColorBrightness(final int channelId, final ChannelUID channelUID, final PercentType command) throws ApiException {
        final Optional<LedState> state = findLedState(channelUID);
        if (state.isPresent()) {
            final LedState ledState = state.get();
            final HSBType newHsbType = new HSBType(
                    ledState.hsb.getHue(),
                    ledState.hsb.getSaturation(),
                    command
            );
            sendNewLedValue(channelUID, channelId, newHsbType, ledState.brightness);
        }
    }

    @Override
    public void changeBrightness(final int channelId, final ChannelUID channelUID, final PercentType command) throws ApiException {
        final Optional<LedState> state = findLedState(channelUID);
        if (state.isPresent()) {
            sendNewLedValue(channelUID, channelId, state.get().hsb, command);
        }
    }

    private void sendNewLedValue(
            final ChannelUID channelUID,
            final int channelId,
            @Nullable final HSBType hsbType,
            @Nullable final PercentType brightness) throws ApiException {
        ChannelExecuteActionRequest action = new ChannelExecuteActionRequest().action(SET_RGBW_PARAMETERS);
        if (hsbType != null) {
            final int colorBrightness = hsbType.getBrightness().intValue();
            final HSBType hsbToConvertToRgb = new HSBType(
                    hsbType.getHue(),
                    hsbType.getSaturation(),
                    PercentType.HUNDRED
            );
            final String rgb = HsbTypeConverter.INSTANCE.convert(hsbToConvertToRgb);
            logger.trace("Changing RGB to {}, color brightness {}%", rgb, colorBrightness);
            action = action.color(rgb).colorBrightness(colorBrightness);
        }
        if (brightness != null) {
            logger.trace("Changing brightness {}%", brightness);
            action = action.brightness(brightness.intValue());
        }

        channelsApi.executeAction(action, channelId);
        ledStates.put(channelUID, new LedState(hsbType, brightness));
    }

    private Optional<LedState> findLedState(final ChannelUID channelUID) {
        final Optional<LedState> ledState = ofNullable(ledStates.get(channelUID));
        if (!ledState.isPresent()) {
            logger.warn("There is no LED state for channel `{}`!", channelUID);
        }
        return ledState;
    }

    private static class LedState {
        @Nullable private final HSBType hsb;
        @Nullable private final PercentType brightness;

        private LedState(@Nullable final HSBType hsb, final @Nullable PercentType brightness) {
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
