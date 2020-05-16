package org.openhab.binding.supla.internal.cloud.functionswitch;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.supla.internal.cloud.AdditionalChannelType;
import org.openhab.binding.supla.internal.cloud.ChannelFunctionDispatcher;
import org.openhab.binding.supla.internal.cloud.ChannelInfo;
import org.openhab.binding.supla.internal.cloud.ChannelInfoParser;
import org.openhab.binding.supla.internal.cloud.executors.LedCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.ControllingChannel;
import pl.grzeslowski.jsupla.api.channel.DepthChannel;
import pl.grzeslowski.jsupla.api.channel.DimmerAndRgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.DimmerChannel;
import pl.grzeslowski.jsupla.api.channel.DistanceChannel;
import pl.grzeslowski.jsupla.api.channel.GateChannel;
import pl.grzeslowski.jsupla.api.channel.HumidityChannel;
import pl.grzeslowski.jsupla.api.channel.NoneChannel;
import pl.grzeslowski.jsupla.api.channel.OnOffChannel;
import pl.grzeslowski.jsupla.api.channel.RgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.RollerShutterChannel;
import pl.grzeslowski.jsupla.api.channel.TemperatureAndHumidityChannel;
import pl.grzeslowski.jsupla.api.channel.TemperatureChannel;
import pl.grzeslowski.jsupla.api.channel.state.BrightnessState;
import pl.grzeslowski.jsupla.api.channel.state.ColorState;
import pl.grzeslowski.jsupla.api.channel.state.DepthState;
import pl.grzeslowski.jsupla.api.channel.state.DistanceState;
import pl.grzeslowski.jsupla.api.channel.state.GateState;
import pl.grzeslowski.jsupla.api.channel.state.HumidityState;
import pl.grzeslowski.jsupla.api.channel.state.OnOffState;
import pl.grzeslowski.jsupla.api.channel.state.Percentage;
import pl.grzeslowski.jsupla.api.channel.state.RollerShutterState;
import pl.grzeslowski.jsupla.api.channel.state.TemperatureState;

import java.util.Optional;

import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.LED_BRIGHTNESS;

@SuppressWarnings("PackageAccessibility")
public class FindStateFunctionSwitch implements ChannelFunctionDispatcher.FunctionSwitch<Optional<? extends State>> {
    private final Logger logger = LoggerFactory.getLogger(FindStateFunctionSwitch.class);
    private final LedCommandExecutor ledCommandExecutor;
    private final ChannelUID channelUID;
    private final ChannelInfoParser channelInfoParser;

    public FindStateFunctionSwitch(LedCommandExecutor ledCommandExecutor, final ChannelUID channelUID, ChannelInfoParser channelInfoParser) {
        this.ledCommandExecutor = ledCommandExecutor;
        this.channelUID = channelUID;
        this.channelInfoParser = channelInfoParser;
    }

    public FindStateFunctionSwitch(LedCommandExecutor ledCommandExecutor, final ChannelUID channelUID) {
        this(ledCommandExecutor, channelUID, ChannelInfoParser.PARSER);
    }

    @Override
    public Optional<? extends State> onNone(final NoneChannel channel) {
        return empty();
    }

    @Override
    public Optional<? extends State> onControllingChannel(final ControllingChannel channel) {
        return channel.findState().map(this::hiType);
    }

    @Override
    public Optional<? extends State> onTemperatureAndHumidityChannel(final TemperatureAndHumidityChannel channel) {
        final ChannelInfo channelInfo = channelInfoParser.parse(channelUID);
        final AdditionalChannelType channelType = channelInfo.getAdditionalChannelType();
        requireNonNull(channelType, "Additional type for channel " + channel + " cannot be null!");
        switch (channelType) {
            case TEMPERATURE:
                return channel.findState()
                               .map(TemperatureState::getTemperatureState)
                               .map(DecimalType::new);
            case HUMIDITY:
                return channel.findState()
                               .map(HumidityState::getHumidityState)
                               .map(Percentage::getPercentage)
                               .map(DecimalType::new);
            default:
                throw new IllegalStateException("Additional type " + channelType + " is not supported for HumidityAndTemperature channel");
        }
    }

    @Override
    public Optional<? extends State> onGateChannel(final GateChannel channel) {
        return channel.findState()
                       .map(GateState::getPosition)
                       .map(p -> p == GateState.Position.OPENED ? ON : OFF);
    }

    @Override
    public Optional<? extends State> onTemperatureChannel(final TemperatureChannel channel) {
        return channel.findState()
                       .map(TemperatureState::getTemperatureState)
                       .map(DecimalType::new);
    }

    @Override
    public Optional<? extends State> onHumidityChannel(final HumidityChannel channel) {
        return channel.findState()
                       .map(HumidityState::getHumidityState)
                       .map(Percentage::getPercentage)
                       .map(DecimalType::new);
    }

    @Override
    public Optional<? extends State> onOnOffChannel(final OnOffChannel channel) {
        return channel.findState().map(this::hiType);
    }

    @Override
    public Optional<? extends State> onRollerShutterChannel(final RollerShutterChannel channel) {
        return channel.findState()
                       .map(RollerShutterState::getOpen)
                       .map(Percentage::getPercentage)
                       .map(PercentType::new);
    }

    @Override
    public Optional<? extends State> onDimmerChannel(final DimmerChannel channel) {
        return channel.findState().flatMap(b -> brightness(b, channel));
    }

    @Override
    public Optional<? extends State> onRgbLightningChannel(final RgbLightningChannel channel) {
        return channel.findState().flatMap(hsv -> hsv(hsv, channel));
    }

    @Override
    public Optional<? extends State> onDimmerAndRgbLightningChannel(final DimmerAndRgbLightningChannel channel) {
        final ChannelInfo channelInfo = channelInfoParser.parse(channelUID);
        AdditionalChannelType channelType = channelInfo.getAdditionalChannelType();
        if (channelType == null) {
            return channel.findState().flatMap(hsv -> hsv(hsv, channel));
        } else if (channelType == LED_BRIGHTNESS) {
            return channel.findState().flatMap(b -> brightness(b, channel));
        } else {
            logger.warn("Do not know how to support {} on dimmer and RGB", channelType);
            return empty();
        }
    }

    private Optional<? extends State> hsv(final ColorState colorState, final Channel channel) {
        final Optional<HSBType> state = of(colorState)
                                                .map(ColorState::getHsv)
                                                .map(hsv -> new HSBType(
                                                        new DecimalType(hsv.getHue()),
                                                        new PercentType((int) round(hsv.getSaturation() * 100)),
                                                        new PercentType((int) round(hsv.getValue() * 100))));
        state.ifPresent(s -> ledCommandExecutor.setLedState(channel, s));
        return state;
    }

    private Optional<? extends State> brightness(final BrightnessState brightnessState, final Channel channel) {
        final Optional<PercentType> state = of(brightnessState)
                                                    .map(BrightnessState::getBrightness)
                                                    .map(Percentage::getPercentage)
                                                    .map(PercentType::new);
        state.ifPresent(s -> ledCommandExecutor.setLedState(channel, s));
        return state;
    }

    @Override
    public Optional<? extends State> onDepthChannel(final DepthChannel channel) {
        return channel.findState()
                       .map(DepthState::getDepth)
                       .map(DecimalType::new);
    }

    @Override
    public Optional<? extends State> onDistanceChannel(final DistanceChannel channel) {
        return channel.findState()
                       .map(DistanceState::getDistanceState)
                       .map(DecimalType::new);
    }

    @Override
    public Optional<? extends State> onDefault(final Channel channel) {
        logger.warn("Does not know how to map `{}` to OpenHAB state for channel {}#{}",
                channel.findState(),
                channel.getClass().getSimpleName(),
                channel.getId());
        return empty();
    }

    private State hiType(OnOffState state) {
        final OnOffState.OnOff hi = state.getOnOffState();
        return hi == OnOffState.OnOff.ON ? ON : OFF;
    }
}
