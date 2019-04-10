package org.openhab.binding.supla.internal.cloud.functionswitch;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.supla.internal.cloud.ChannelFunctionDispatcher;
import org.openhab.binding.supla.internal.cloud.HsbTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.generated.model.Channel;
import pl.grzeslowski.jsupla.api.generated.model.ChannelState;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;

public class FindStateFunctionSwitch implements ChannelFunctionDispatcher.FunctionSwitch<Optional<State>> {
    private final Logger logger = LoggerFactory.getLogger(FindStateFunctionSwitch.class);
    private final pl.grzeslowski.jsupla.api.generated.model.Channel channel;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private final Optional<ChannelState> state;

    public FindStateFunctionSwitch(final Channel channel) {
        this.channel = requireNonNull(channel);
        state = of(channel.getState());
    }

    @Override
    public Optional<State> onNone() {
        return Optional.empty();
    }

    @Override
    public Optional<State> onControllingTheGatewayLock() {
        return hiType();
    }

    @Override
    public Optional<State> onControllingTheGate() {
        return hiType();
    }

    @Override
    public Optional<State> onControllingTheGarageDoor() {
        return hiType();
    }

    @Override
    public Optional<State> onThermometer() {
        return state.map(this::findTemperature).map(DecimalType::new);
    }

    @Override
    public Optional<State> onHumidity() {
        return state.map(this::findHumidity).map(DecimalType::new);
    }

    @Override
    public Optional<State> onHumidityAndTemperature() {
        return state.map(s -> findTemperature(s) + " °C" + findHumidity(s) + "%").map(StringType::new);
    }

    private BigDecimal findTemperature(ChannelState channelState) {
        return findValueWithAdjustment(channelState.getTemperature(), channel.getParam2());
    }

    private BigDecimal findHumidity(ChannelState channelState) {
        return findValueWithAdjustment(channelState.getHumidity(), channel.getParam3());
    }

    private BigDecimal findValueWithAdjustment(BigDecimal value, Integer adjustment) {
        return Optional.ofNullable(adjustment)
                       .map(v -> v / 100)
                       .map(BigDecimal::new)
                       .orElse(BigDecimal.ZERO)
                       .add(value);
    }

    @Override
    public Optional<State> onOpeningSensorGateway() {
        return hiType();
    }

    @Override
    public Optional<State> onOpeningSensorGate() {
        return hiType();
    }

    @Override
    public Optional<State> onOpeningSensorGarageDoor() {
        return hiType();
    }

    @Override
    public Optional<State> onNoLiquidSensor() {
        return hiType();
    }

    @Override
    public Optional<State> onControllingTheDoorLock() {
        return hiType();
    }

    @Override
    public Optional<State> onOpeningSensorDoor() {
        return hiType();
    }

    @Override
    public Optional<State> onControllingTheRollerShutter() {
        return state.map(ChannelState::getShut).map(PercentType::new);
    }

    @Override
    public Optional<State> onOpeningSensorRollerShutter() {
        return hiType();
    }

    @Override
    public Optional<State> onPowerSwitch() {
        return onOffType();
    }

    @Override
    public Optional<State> onLightSwitch() {
        return onOffType();
    }

    private Optional<State> onOffType() {
        return state.map(ChannelState::getOn).map(on -> on ? ON : OFF);
    }

    @Override
    public Optional<State> onDimmer() {
        return state.map(ChannelState::getBrightness)
                       .map(b -> b / 100.0)
                       .map(DecimalType::new);
    }

    @Override
    public Optional<State> onRgbLighting() {
        return state.map(s -> HsbTypeConverter.INSTANCE.toHsbType(s.getColor(), s.getColorBrightness()));
    }

    @Override
    public Optional<State> onDimmerAndRgbLightning() {
        return state.map(s -> HsbTypeConverter.INSTANCE.toHsbType(s.getColor(), s.getColorBrightness(), s.getBrightness()));
    }

    @Override
    public Optional<State> onDepthSensor() {
        return state.map(ChannelState::getDepth).map(DecimalType::new);
    }

    @Override
    public Optional<State> onDistanceSensor() {
        return state.map(ChannelState::getDistance).map(DecimalType::new);
    }

    @Override
    public Optional<State> onOpeningSensorWindow() {
        return hiType();
    }

    @Override
    public Optional<State> onMailSensor() {
        return hiType();
    }

    @Override
    public Optional<State> onWindSensor() {
        return Optional.empty();
    }

    @Override
    public Optional<State> onPressureSensor() {
        return Optional.empty();
    }

    @Override
    public Optional<State> onRainSensor() {
        return Optional.empty();
    }

    @Override
    public Optional<State> onWeightSensor() {
        return Optional.empty();
    }

    @Override
    public Optional<State> onWeatherStation() {
        return Optional.empty();
    }

    @Override
    public Optional<State> onStaircaseTimer() {
        return onOffType();
    }

    @Override
    public Optional<State> onDefault() {
        logger.warn("Does not know how to map `{}` to OpenHAB state", channel.getState());
        return Optional.empty();
    }

    private Optional<State> hiType() {
        boolean param2Present = channel.getParam2() != null && channel.getParam2() > 0;
        if (param2Present || !channel.getType().isOutput()) {
            return state.map(ChannelState::getHi).map(hi -> hi ? ON : OFF);
        } else {
            return empty();
        }
    }
}
