package org.openhab.binding.supla.internal.cloud;

import pl.grzeslowski.jsupla.api.generated.model.Channel;
import pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames;

@SuppressWarnings("PackageAccessibility")
public class ChannelFunctionDispatcher {
    public static final ChannelFunctionDispatcher DISPATCHER = new ChannelFunctionDispatcher();

    public <T> T dispatch(Channel channel, FunctionSwitch<T> functionSwitch) {
        return dispatch(channel.getFunction().getName(), functionSwitch);
    }

    @SuppressWarnings("WeakerAccess")
    public <T> T dispatch(ChannelFunctionEnumNames function, FunctionSwitch<T> functionSwitch) {
        switch (function) {
            case NONE:
                return functionSwitch.onNone();
            case CONTROLLINGTHEGATEWAYLOCK:
                return functionSwitch.onControllingTheGatewayLock();
            case CONTROLLINGTHEGATE:
                return functionSwitch.onControllingTheGate();
            case CONTROLLINGTHEGARAGEDOOR:
                return functionSwitch.onControllingTheGarageDoor();
            case THERMOMETER:
                return functionSwitch.onThermometer();
            case HUMIDITY:
                return functionSwitch.onHumidity();
            case HUMIDITYANDTEMPERATURE:
                return functionSwitch.onHumidityAndTemperature();
            case OPENINGSENSOR_GATEWAY:
                return functionSwitch.onOpeningSensorGateway();
            case OPENINGSENSOR_GATE:
                return functionSwitch.onOpeningSensorGate();
            case OPENINGSENSOR_GARAGEDOOR:
                return functionSwitch.onOpeningSensorGarageDoor();
            case NOLIQUIDSENSOR:
                return functionSwitch.onNoLiquidSensor();
            case CONTROLLINGTHEDOORLOCK:
                return functionSwitch.onControllingTheDoorLock();
            case OPENINGSENSOR_DOOR:
                return functionSwitch.onOpeningSensorDoor();
            case CONTROLLINGTHEROLLERSHUTTER:
                return functionSwitch.onControllingTheRollerShutter();
            case OPENINGSENSOR_ROLLERSHUTTER:
                return functionSwitch.onOpeningSensorRollerShutter();
            case POWERSWITCH:
                return functionSwitch.onPowerSwitch();
            case LIGHTSWITCH:
                return functionSwitch.onLightSwitch();
            case DIMMER:
                return functionSwitch.onDimmer();
            case RGBLIGHTING:
                return functionSwitch.onRgbLighting();
            case DIMMERANDRGBLIGHTING:
                return functionSwitch.onDimmerAndRgbLightning();
            case DEPTHSENSOR:
                return functionSwitch.onDepthSensor();
            case DISTANCESENSOR:
                return functionSwitch.onDistanceSensor();
            case OPENINGSENSOR_WINDOW:
                return functionSwitch.onOpeningSensorWindow();
            case MAILSENSOR:
                return functionSwitch.onMailSensor();
            case WINDSENSOR:
                return functionSwitch.onWindSensor();
            case PRESSURESENSOR:
                return functionSwitch.onPressureSensor();
            case RAINSENSOR:
                return functionSwitch.onRainSensor();
            case WEIGHTSENSOR:
                return functionSwitch.onWeightSensor();
            case WEATHER_STATION:
                return functionSwitch.onWeatherStation();
            case STAIRCASETIMER:
                return functionSwitch.onStaircaseTimer();
            default:
                return functionSwitch.onDefault();
        }
    }

    public interface FunctionSwitch<T> {
        T onNone();

        T onControllingTheGatewayLock();

        T onControllingTheGate();

        T onControllingTheGarageDoor();

        T onThermometer();

        T onHumidity();

        T onHumidityAndTemperature();

        T onOpeningSensorGateway();

        T onOpeningSensorGate();

        T onOpeningSensorGarageDoor();

        T onNoLiquidSensor();

        T onControllingTheDoorLock();

        T onOpeningSensorDoor();

        T onControllingTheRollerShutter();

        T onOpeningSensorRollerShutter();

        T onPowerSwitch();

        T onLightSwitch();

        T onDimmer();

        T onRgbLighting();

        T onDimmerAndRgbLightning();

        T onDepthSensor();

        T onDistanceSensor();

        T onOpeningSensorWindow();

        T onMailSensor();

        T onWindSensor();

        T onPressureSensor();

        T onRainSensor();

        T onWeightSensor();

        T onWeatherStation();

        T onStaircaseTimer();

        T onDefault();
    }
}
