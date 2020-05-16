package org.openhab.binding.supla.internal.cloud;

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

@SuppressWarnings("PackageAccessibility")
public class ChannelFunctionDispatcher {
    public static final ChannelFunctionDispatcher DISPATCHER = new ChannelFunctionDispatcher();

    public <T> T dispatch(Channel channel, FunctionSwitch<T> functionSwitch) {
        if (channel instanceof NoneChannel) {
            return functionSwitch.onNone((NoneChannel) channel);
        } else if (channel instanceof ControllingChannel) {
            return functionSwitch.onControllingChannel((ControllingChannel) channel);
        } else if (channel instanceof TemperatureAndHumidityChannel) {
            return functionSwitch.onTemperatureAndHumidityChannel((TemperatureAndHumidityChannel) channel);
        } else if (channel instanceof GateChannel) {
            return functionSwitch.onGateChannel((GateChannel) channel);
        } else if (channel instanceof TemperatureChannel) {
            return functionSwitch.onTemperatureChannel((TemperatureChannel) channel);
        } else if (channel instanceof HumidityChannel) {
            return functionSwitch.onHumidityChannel((HumidityChannel) channel);
        } else if (channel instanceof OnOffChannel) {
            return functionSwitch.onOnOffChannel((OnOffChannel) channel);
        } else if (channel instanceof RollerShutterChannel) {
            return functionSwitch.onRollerShutterChannel((RollerShutterChannel) channel);
        } else if (channel instanceof DimmerChannel) {
            return functionSwitch.onDimmerChannel((DimmerChannel) channel);
        } else if (channel instanceof RgbLightningChannel) {
            return functionSwitch.onRgbLightningChannel((RgbLightningChannel) channel);
        } else if (channel instanceof DimmerAndRgbLightningChannel) {
            return functionSwitch.onDimmerAndRgbLightningChannel((DimmerAndRgbLightningChannel) channel);
        } else if (channel instanceof DepthChannel) {
            return functionSwitch.onDepthChannel((DepthChannel) channel);
        } else if (channel instanceof DistanceChannel) {
            return functionSwitch.onDistanceChannel((DistanceChannel) channel);
        } else {
            return functionSwitch.onDefault(channel);
        }
    }

    public interface FunctionSwitch<T> {
        T onNone(NoneChannel channel);

        T onControllingChannel(ControllingChannel channel);

        T onTemperatureAndHumidityChannel(TemperatureAndHumidityChannel channel);

        T onGateChannel(GateChannel channel);

        T onTemperatureChannel(TemperatureChannel channel);

        T onHumidityChannel(HumidityChannel channel);

        T onOnOffChannel(OnOffChannel channel);

        T onRollerShutterChannel(RollerShutterChannel channel);

        T onDimmerChannel(DimmerChannel channel);

        T onRgbLightningChannel(RgbLightningChannel channel);

        T onDimmerAndRgbLightningChannel(DimmerAndRgbLightningChannel channel);

        T onDepthChannel(DepthChannel channel);

        T onDistanceChannel(DistanceChannel channel);

        T onDefault(Channel channel);
    }
}
