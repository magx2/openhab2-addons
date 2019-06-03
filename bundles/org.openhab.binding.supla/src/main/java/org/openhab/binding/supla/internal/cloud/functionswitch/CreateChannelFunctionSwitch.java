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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
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
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TEMPERATURE_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TOGGLE_GAT_CHANNEL_ID;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.HUMIDITY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.TEMPERATURE;

@SuppressWarnings("PackageAccessibility")
public class CreateChannelFunctionSwitch implements ChannelFunctionDispatcher.FunctionSwitch<List<Channel>> {
    private final Logger logger = LoggerFactory.getLogger(CreateChannelFunctionSwitch.class);
    private final ThingUID thingUID;

    public CreateChannelFunctionSwitch(final ThingUID thingUID) {
        this.thingUID = requireNonNull(thingUID);
    }

    @Override
    public List<Channel> onNone(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onControllingTheGatewayLock(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onControllingTheGate(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createToggleGateChannel(channel);
    }

    @Override
    public List<Channel> onControllingTheGarageDoor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createToggleGateChannel(channel);
    }

    @Override
    public List<Channel> onThermometer(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, TEMPERATURE_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onHumidity(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, HUMIDITY_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onHumidityAndTemperature(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return asList(
                createChannel(TEMPERATURE_CHANNEL_ID, "Number", channel.getId() + TEMPERATURE.getSuffix(), "Temperature"),
                createChannel(HUMIDITY_CHANNEL_ID, "Number", channel.getId() + HUMIDITY.getSuffix(), "Humidity")
        );
    }

    @Override
    public List<Channel> onOpeningSensorGateway(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onOpeningSensorGate(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onOpeningSensorGarageDoor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onNoLiquidSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onControllingTheDoorLock(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onOpeningSensorDoor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onControllingTheRollerShutter(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, ROLLER_SHUTTER_CHANNEL_ID, "Rollershutter"));
    }

    @Override
    public List<Channel> onOpeningSensorRollerShutter(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onPowerSwitch(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onLightSwitch(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, LIGHT_CHANNEL_ID, "Switch"));
    }

    @Override
    public List<Channel> onDimmer(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, DIMMER_CHANNEL_ID, "Dimmer"));
    }

    @Override
    public List<Channel> onRgbLighting(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createLedChannels(channel);
    }

    @Override
    public List<Channel> onDimmerAndRgbLightning(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        final List<Channel> ledChannels = createLedChannels(channel);
        final List<Channel> channels = new ArrayList<>(ledChannels);
        final Channel brightnessChannel = createChannel(DIMMER_CHANNEL_ID, "Dimmer", channel.getId() + "_brightness", "Brightness");
        channels.add(brightnessChannel);
        return channels;
    }

    @Override
    public List<Channel> onDepthSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, DECIMAL_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onDistanceSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, DECIMAL_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onOpeningSensorWindow(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onMailSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onWindSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onPressureSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onRainSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onWeightSensor(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onWeatherStation(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onStaircaseTimer(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onDefault(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        logger.warn("Does not know type of this `{}` function", channel.getFunction().getName());
        return emptyList();
    }

    private Channel createChannel(
            final String id,
            final String acceptedItemType,
            final String channelId,
            final String caption) {
        final ChannelUID channelUid = new ChannelUID(thingUID, channelId);
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(SuplaBindingConstants.BINDING_ID, id);

        final ChannelBuilder channelBuilder = ChannelBuilder.create(channelUid, acceptedItemType)
                                                      .withType(channelTypeUID);

        if (!isNullOrEmpty(caption)) {
            channelBuilder.withLabel(caption);
        }
        return channelBuilder.build();
    }

    private Channel createChannel(
            final pl.grzeslowski.jsupla.api.generated.model.Channel channel,
            final String id,
            final String acceptedItemType) {
        return createChannel(id, acceptedItemType, String.valueOf(channel.getId()), channel.getCaption());
    }

    private List<Channel> createSwitchChannel(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        if (channel.getType().isOutput()) {
            return singletonList(createChannel(channel, SWITCH_CHANNEL_ID, "Switch"));
        } else {
            return singletonList(createChannel(channel, SWITCH_CHANNEL_RO_ID, "Switch"));
        }
    }

    private List<Channel> createToggleGateChannel(pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return singletonList(createChannel(channel, TOGGLE_GAT_CHANNEL_ID, "Switch"));
    }

    private List<Channel> createLedChannels(final pl.grzeslowski.jsupla.api.generated.model.Channel channel) {
        return Collections.singletonList(
                createChannel(channel, RGB_CHANNEL_ID, "Color"));
    }
}
