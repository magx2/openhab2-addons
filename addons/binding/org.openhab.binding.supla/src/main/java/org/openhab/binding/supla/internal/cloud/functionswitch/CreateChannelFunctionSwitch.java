package org.openhab.binding.supla.internal.cloud.functionswitch;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.supla.SuplaBindingConstants;
import org.openhab.binding.supla.internal.cloud.ChannelFunctionDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.DECIMAL_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.DIMMER_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.HUMIDITY_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.LIGHT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.RGB_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.ROLLER_SHUTTER_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.SWITCH_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.SWITCH_CHANNEL_RO_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TEMPERATURE_AND_HUMIDITY_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TEMPERATURE_CHANNEL_ID;

@SuppressWarnings("PackageAccessibility")
public class CreateChannelFunctionSwitch implements ChannelFunctionDispatcher.FunctionSwitch<List<Channel>> {
    private final Logger logger = LoggerFactory.getLogger(CreateChannelFunctionSwitch.class);
    private final pl.grzeslowski.jsupla.api.generated.model.Channel channel;
    private final ThingUID thingUID;

    public CreateChannelFunctionSwitch(final pl.grzeslowski.jsupla.api.generated.model.Channel channel, final ThingUID thingUID) {
        this.channel = requireNonNull(channel);
        this.thingUID = requireNonNull(thingUID);
    }

    public pl.grzeslowski.jsupla.api.generated.model.Channel getChannel() {
        return channel;
    }

    @Override
    public List<Channel> onNone() {
        return emptyList();
    }

    @Override
    public List<Channel> onControllingTheGatewayLock() {
        return createSwitchChannel();
    }

    @Override
    public List<Channel> onControllingTheGate() {
        return createSwitchChannel();
    }

    @Override
    public List<Channel> onControllingTheGarageDoor() {
        return createSwitchChannel();
    }

    @Override
    public List<Channel> onThermometer() {
        return singletonList(createChannel(TEMPERATURE_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onHumidity() {
        return singletonList(createChannel(HUMIDITY_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onHumidityAndTemperature() {
        return singletonList(createChannel(TEMPERATURE_AND_HUMIDITY_CHANNEL_ID, "String"));
    }

    @Override
    public List<Channel> onOpeningSensorGateway() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onOpeningSensorGate() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onOpeningSensorGarageDoor() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onNoLiquidSensor() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onControllingTheDoorLock() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onOpeningSensorDoor() {
        return emptyList();
    }

    @Override
    public List<Channel> onControllingTheRollerShutter() {
        return singletonList(createChannel(ROLLER_SHUTTER_CHANNEL_ID, "Rollershutter"));
    }

    @Override
    public List<Channel> onOpeningSensorRollerShutter() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onPowerSwitch() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onLightSwitch() {
        return singletonList(createChannel(LIGHT_CHANNEL_ID, "Switch"));
    }

    @Override
    public List<Channel> onDimmer() {
        return singletonList(createChannel(DIMMER_CHANNEL_ID, "Dimmer"));
    }

    @Override
    public List<Channel> onRgbLighting() {
        return singletonList(createChannel(RGB_CHANNEL_ID, "Color"));
    }

    @Override
    public List<Channel> onDimmerAndRgbLightning() {
        return singletonList(createChannel(RGB_CHANNEL_ID, "Color"));
    }

    @Override
    public List<Channel> onDepthSensor() {
        return singletonList(createChannel(DECIMAL_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onDistanceSensor() {
        return singletonList(createChannel(DECIMAL_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onOpeningSensorWindow() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onMailSensor() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onWindSensor() {
        return emptyList();
    }

    @Override
    public List<Channel> onPressureSensor() {
        return emptyList();
    }

    @Override
    public List<Channel> onRainSensor() {
        return emptyList();
    }

    @Override
    public List<Channel> onWeightSensor() {
        return emptyList();
    }

    @Override
    public List<Channel> onWeatherStation() {
        return emptyList();
    }

    @Override
    public List<Channel> onStaircaseTimer() {
        return createSwichChannel();
    }

    @Override
    public List<Channel> onDefault() {
        logger.warn("Does not know type of this `{}` function", channel.getFunction().getName());
        return emptyList();
    }

    private Channel createChannel(
            final String id,
            final String acceptedItemType,
            final String channelId) {
        final ChannelUID channelUid = new ChannelUID(thingUID, channelId);
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(SuplaBindingConstants.BINDING_ID, id);

        final ChannelBuilder channelBuilder = ChannelBuilder.create(channelUid, acceptedItemType)
                                                      .withType(channelTypeUID);

        if (!isNullOrEmpty(channel.getCaption())) {
            channelBuilder.withLabel(channel.getCaption());
        }
        return channelBuilder.build();
    }

    private Channel createChannel(String id, final String acceptedItemType) {
        return createChannel(id, acceptedItemType, valueOf(channel.getId()));
    }

    private List<Channel> createSwitchChannel() {
        boolean param2Present = channel.getParam2() != null && channel.getParam2() > 0;
        if (param2Present) {
            if (channel.getType().isOutput()) {
                return singletonList(createChannel(SWITCH_CHANNEL_ID, "Switch"));
            } else {
                return singletonList(createChannel(SWITCH_CHANNEL_RO_ID, "Switch"));
            }
        } else {
            logger.debug("Channel with function `{}` has not param2! {}", channel.getFunction().getName(), channel);
            return emptyList();
        }
    }

    private List<Channel> createSwichChannel() {
        if (channel.getType().isOutput()) {
            return singletonList(createChannel(SWITCH_CHANNEL_ID, "Switch"));
        } else {
            return singletonList(createChannel(SWITCH_CHANNEL_RO_ID, "Switch"));
        }
    }

}
