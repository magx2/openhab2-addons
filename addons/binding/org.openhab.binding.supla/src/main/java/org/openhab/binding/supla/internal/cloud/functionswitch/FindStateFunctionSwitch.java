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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;

@SuppressWarnings("PackageAccessibility")
public class FindStateFunctionSwitch implements ChannelFunctionDispatcher.FunctionSwitch<Optional<State>> {
    private final Logger logger = LoggerFactory.getLogger(FindStateFunctionSwitch.class);

    @Override
    public Optional<State> onNone(Channel channel) {
        return Optional.empty();
    }

    @Override
    public Optional<State> onControllingTheGatewayLock(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onControllingTheGate(Channel channel) {
        return optionalHiType(channel);
    }

    @Override
    public Optional<State> onControllingTheGarageDoor(Channel channel) {
        return optionalHiType(channel);
    }

    @Override
    public Optional<State> onThermometer(Channel channel) {
        return of(channel).map(Channel::getState).map(s -> findTemperature(s, channel.getParam2())).map(DecimalType::new);
    }

    @Override
    public Optional<State> onHumidity(Channel channel) {
        return of(channel).map(Channel::getState).map(channelState -> findHumidity(channelState, channel.getParam3())).map(DecimalType::new);
    }

    @Override
    public Optional<State> onHumidityAndTemperature(Channel channel) {
        return of(channel).map(Channel::getState).map(s -> findTemperature(s, channel.getParam2()) + " °C" + findHumidity(s, channel.getParam3()) + "%").map(StringType::new);
    }

    private BigDecimal findTemperature(ChannelState channelState, Integer param2) {
        return findValueWithAdjustment(channelState.getTemperature(), param2);
    }

    private BigDecimal findHumidity(ChannelState channelState, Integer param3) {
        return findValueWithAdjustment(channelState.getHumidity(), param3);
    }

    private BigDecimal findValueWithAdjustment(BigDecimal value, Integer adjustment) {
        return Optional.ofNullable(adjustment)
                       .map(v -> v / 100)
                       .map(BigDecimal::new)
                       .orElse(BigDecimal.ZERO)
                       .add(value);
    }

    @Override
    public Optional<State> onOpeningSensorGateway(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onOpeningSensorGate(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onOpeningSensorGarageDoor(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onNoLiquidSensor(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onControllingTheDoorLock(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onOpeningSensorDoor(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onControllingTheRollerShutter(Channel channel) {
        return of(channel).map(Channel::getState).map(ChannelState::getShut).map(PercentType::new);
    }

    @Override
    public Optional<State> onOpeningSensorRollerShutter(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onPowerSwitch(Channel channel) {
        return onOffType(channel);
    }

    @Override
    public Optional<State> onLightSwitch(Channel channel) {
        return onOffType(channel);
    }

    private Optional<State> onOffType(Channel channel) {
        return of(channel).map(Channel::getState).map(ChannelState::getOn).map(on -> on ? ON : OFF);
    }

    @Override
    public Optional<State> onDimmer(Channel channel) {
        return of(channel).map(Channel::getState).map(ChannelState::getBrightness)
                       .map(b -> b / 100.0)
                       .map(DecimalType::new);
    }

    @Override
    public Optional<State> onRgbLighting(Channel channel) {
        return of(channel).map(Channel::getState).map(s -> HsbTypeConverter.INSTANCE.toHsbType(s.getColor(), s.getColorBrightness()));
    }

    @Override
    public Optional<State> onDimmerAndRgbLightning(Channel channel) {
        return of(channel).map(Channel::getState).map(s -> HsbTypeConverter.INSTANCE.toHsbType(s.getColor(), s.getColorBrightness(), s.getBrightness()));
    }

    @Override
    public Optional<State> onDepthSensor(Channel channel) {
        return of(channel).map(Channel::getState).map(ChannelState::getDepth).map(DecimalType::new);
    }

    @Override
    public Optional<State> onDistanceSensor(Channel channel) {
        return of(channel).map(Channel::getState).map(ChannelState::getDistance).map(DecimalType::new);
    }

    @Override
    public Optional<State> onOpeningSensorWindow(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onMailSensor(Channel channel) {
        return hiType(channel);
    }

    @Override
    public Optional<State> onWindSensor(Channel channel) {
        return Optional.empty();
    }

    @Override
    public Optional<State> onPressureSensor(Channel channel) {
        return Optional.empty();
    }

    @Override
    public Optional<State> onRainSensor(Channel channel) {
        return Optional.empty();
    }

    @Override
    public Optional<State> onWeightSensor(Channel channel) {
        return Optional.empty();
    }

    @Override
    public Optional<State> onWeatherStation(Channel channel) {
        return Optional.empty();
    }

    @Override
    public Optional<State> onStaircaseTimer(Channel channel) {
        return onOffType(channel);
    }

    @Override
    public Optional<State> onDefault(Channel channel) {
        logger.warn("Does not know how to map `{}` to OpenHAB state", channel.getState());
        return Optional.empty();
    }

    private Optional<State> hiType(Channel channel) {
        boolean invertedLogic = channel.getParam3() != null && channel.getParam3() > 1;
        return of(channel)
                       .map(Channel::getState)
                       .map(ChannelState::getHi)
                       .map(hi -> invertedLogic ? !hi : hi)
                       .map(hi -> hi ? ON : OFF);
    }

    /**
     * For `CONTROLLINGTHEGATE` and `CONTROLLINGTHEGARAGEDOOR` `hi` exists only when `param2` is set.
     * <p>
     * From doc:
     * <p>
     * "hi is either true or false depending on paired opening sensor state; the hi value is provided only if the
     * channel has param2 set (i.e. has opening sensor chosen); partial_hi is either true or false depending on paired
     * secondary opening sensor state; the partial_hi value is provided only if the channel has param3 set (i.e. has
     * secondary opening sensor chosen)"
     * <p>
     * https://github.com/SUPLA/supla-cloud/wiki/Channel-Functions-states
     */
    private Optional<State> optionalHiType(Channel channel) {
        boolean invertedLogic = channel.getParam3() != null && channel.getParam3() > 1;
        boolean param2Present = channel.getParam2() != null && channel.getParam2() > 0;
        if (param2Present || !channel.getType().isOutput()) {
            return of(channel)
                           .map(Channel::getState)
                           .map(ChannelState::getHi)
                           .map(hi -> invertedLogic ? !hi : hi)
                           .map(hi -> hi ? ON : OFF);
        } else {
            return empty();
        }
    }
}
