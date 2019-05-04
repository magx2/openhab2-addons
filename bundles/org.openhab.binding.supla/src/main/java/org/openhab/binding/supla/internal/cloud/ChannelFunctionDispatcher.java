package org.openhab.binding.supla.internal.cloud;

import pl.grzeslowski.jsupla.api.generated.model.Channel;

@SuppressWarnings("PackageAccessibility")
public class ChannelFunctionDispatcher {
    public static final ChannelFunctionDispatcher DISPATCHER = new ChannelFunctionDispatcher();

    public <T> T dispatch(Channel channel, FunctionSwitch<T> functionSwitch) {
        switch (channel.getFunction().getName()) {
            case NONE:
                return functionSwitch.onNone(channel);
            case CONTROLLINGTHEGATEWAYLOCK:
                return functionSwitch.onControllingTheGatewayLock(channel);
            case CONTROLLINGTHEGATE:
                return functionSwitch.onControllingTheGate(channel);
            case CONTROLLINGTHEGARAGEDOOR:
                return functionSwitch.onControllingTheGarageDoor(channel);
            case THERMOMETER:
                return functionSwitch.onThermometer(channel);
            case HUMIDITY:
                return functionSwitch.onHumidity(channel);
            case HUMIDITYANDTEMPERATURE:
                return functionSwitch.onHumidityAndTemperature(channel);
            case OPENINGSENSOR_GATEWAY:
                return functionSwitch.onOpeningSensorGateway(channel);
            case OPENINGSENSOR_GATE:
                return functionSwitch.onOpeningSensorGate(channel);
            case OPENINGSENSOR_GARAGEDOOR:
                return functionSwitch.onOpeningSensorGarageDoor(channel);
            case NOLIQUIDSENSOR:
                return functionSwitch.onNoLiquidSensor(channel);
            case CONTROLLINGTHEDOORLOCK:
                return functionSwitch.onControllingTheDoorLock(channel);
            case OPENINGSENSOR_DOOR:
                return functionSwitch.onOpeningSensorDoor(channel);
            case CONTROLLINGTHEROLLERSHUTTER:
                return functionSwitch.onControllingTheRollerShutter(channel);
            case OPENINGSENSOR_ROLLERSHUTTER:
                return functionSwitch.onOpeningSensorRollerShutter(channel);
            case POWERSWITCH:
                return functionSwitch.onPowerSwitch(channel);
            case LIGHTSWITCH:
                return functionSwitch.onLightSwitch(channel);
            case DIMMER:
                return functionSwitch.onDimmer(channel);
            case RGBLIGHTING:
                return functionSwitch.onRgbLighting(channel);
            case DIMMERANDRGBLIGHTING:
                return functionSwitch.onDimmerAndRgbLightning(channel);
            case DEPTHSENSOR:
                return functionSwitch.onDepthSensor(channel);
            case DISTANCESENSOR:
                return functionSwitch.onDistanceSensor(channel);
            case OPENINGSENSOR_WINDOW:
                return functionSwitch.onOpeningSensorWindow(channel);
            case MAILSENSOR:
                return functionSwitch.onMailSensor(channel);
            case WINDSENSOR:
                return functionSwitch.onWindSensor(channel);
            case PRESSURESENSOR:
                return functionSwitch.onPressureSensor(channel);
            case RAINSENSOR:
                return functionSwitch.onRainSensor(channel);
            case WEIGHTSENSOR:
                return functionSwitch.onWeightSensor(channel);
            case WEATHER_STATION:
                return functionSwitch.onWeatherStation(channel);
            case STAIRCASETIMER:
                return functionSwitch.onStaircaseTimer(channel);
            default:
                return functionSwitch.onDefault(channel);
        }
    }

    public interface FunctionSwitch<T> {
        T onNone(Channel channel);

        T onControllingTheGatewayLock(Channel channel);

        T onControllingTheGate(Channel channel);

        T onControllingTheGarageDoor(Channel channel);

        T onThermometer(Channel channel);

        T onHumidity(Channel channel);

        T onHumidityAndTemperature(Channel channel);

        T onOpeningSensorGateway(Channel channel);

        T onOpeningSensorGate(Channel channel);

        T onOpeningSensorGarageDoor(Channel channel);

        T onNoLiquidSensor(Channel channel);

        T onControllingTheDoorLock(Channel channel);

        T onOpeningSensorDoor(Channel channel);

        T onControllingTheRollerShutter(Channel channel);

        T onOpeningSensorRollerShutter(Channel channel);

        T onPowerSwitch(Channel channel);

        T onLightSwitch(Channel channel);

        T onDimmer(Channel channel);

        T onRgbLighting(Channel channel);

        T onDimmerAndRgbLightning(Channel channel);

        T onDepthSensor(Channel channel);

        T onDistanceSensor(Channel channel);

        T onOpeningSensorWindow(Channel channel);

        T onMailSensor(Channel channel);

        T onWindSensor(Channel channel);

        T onPressureSensor(Channel channel);

        T onRainSensor(Channel channel);

        T onWeightSensor(Channel channel);

        T onWeatherStation(Channel channel);

        T onStaircaseTimer(Channel channel);

        T onDefault(Channel channel);
    }
}
