package org.openhab.binding.supla.internal.cloud.functionswitch;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.supla.SuplaBindingConstants;
import org.openhab.binding.supla.internal.cloud.AdditionalChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jsupla.api.channel.ChannelDispatcher;
import pl.grzeslowski.jsupla.api.channel.ControllingChannel;
import pl.grzeslowski.jsupla.api.channel.DepthChannel;
import pl.grzeslowski.jsupla.api.channel.DimmerAndRgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.DimmerChannel;
import pl.grzeslowski.jsupla.api.channel.DistanceChannel;
import pl.grzeslowski.jsupla.api.channel.ElectricityMeterChannel;
import pl.grzeslowski.jsupla.api.channel.GateChannel;
import pl.grzeslowski.jsupla.api.channel.HumidityChannel;
import pl.grzeslowski.jsupla.api.channel.NoneChannel;
import pl.grzeslowski.jsupla.api.channel.OnOffChannel;
import pl.grzeslowski.jsupla.api.channel.RgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.RollerShutterChannel;
import pl.grzeslowski.jsupla.api.channel.TemperatureAndHumidityChannel;
import pl.grzeslowski.jsupla.api.channel.TemperatureChannel;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.DECIMAL_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.DIMMER_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.EXTRA_LIGHT_ACTIONS_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.HUMIDITY_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.PRICE_PER_UNIT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.FREQUENCY_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.NUMBER_OF_PHASES_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.POWER_ACTIVE_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.POWER_APPARENT_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.POWER_REACTIVE_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.TOTAL_FORWARD_ACTIVE_ENERGY_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.TOTAL_FORWARD_REACTIVE_ENERGY_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.TOTAL_REVERSE_ACTIVE_ENERGY_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.Phase.TOTAL_REVERSE_REACTIVE_ENERGY_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.RGB_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.ROLLER_SHUTTER_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.SWITCH_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.SWITCH_CHANNEL_RO_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TEMPERATURE_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TOGGLE_GAT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.Channels.TOTAL_COST_CHANNEL_ID;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.EXTRA_LIGHT_ACTIONS;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.HUMIDITY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_FREQUENCY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_NUMBER;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_POWER_ACTIVE;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_POWER_APPARENT;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_POWER_REACTIVE;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_TOTAL_FORWARD_ACTIVE_ENERGY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_TOTAL_FORWARD_REACTIVE_ENERGY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_TOTAL_REVERSE_ACTIVE_ENERGY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PHASE_TOTAL_REVERSE_REACTIVE_ENERGY;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.PRICE_PER_UNIT;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.TEMPERATURE;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.TOTAL_COST;

@SuppressWarnings("PackageAccessibility")
public class CreateChannelFunctionSwitch implements ChannelDispatcher.FunctionSwitch<List<Channel>> {
    private final Logger logger = LoggerFactory.getLogger(CreateChannelFunctionSwitch.class);
    private final ThingUID thingUID;

    public CreateChannelFunctionSwitch(final ThingUID thingUID) {
        this.thingUID = requireNonNull(thingUID);
    }

    @Override
    public List<Channel> onNone(final NoneChannel channel) {
        return emptyList();
    }

    @Override
    public List<Channel> onControllingChannel(final ControllingChannel channel) {
        return createToggleGateChannel(channel);
    }

    @Override
    public List<Channel> onTemperatureAndHumidityChannel(final TemperatureAndHumidityChannel channel) {
        return asList(
                createChannel(TEMPERATURE_CHANNEL_ID, "Number", channel.getId() + TEMPERATURE.getSuffix(), "Temperature"),
                createChannel(HUMIDITY_CHANNEL_ID, "Number", channel.getId() + HUMIDITY.getSuffix(), "Humidity")
        );
    }

    @Override
    public List<Channel> onGateChannel(final GateChannel channel) {
        return createToggleGateChannel(channel);
    }

    @Override
    public List<Channel> onTemperatureChannel(final TemperatureChannel channel) {
        return singletonList(createChannel(channel, TEMPERATURE_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onHumidityChannel(final HumidityChannel channel) {
        return singletonList(createChannel(HUMIDITY_CHANNEL_ID, "Number", channel.getId() + HUMIDITY.getSuffix(), "Humidity"));
    }

    @Override
    public List<Channel> onOnOffChannel(final OnOffChannel channel) {
        return createSwitchChannel(channel);
    }

    @Override
    public List<Channel> onRollerShutterChannel(final RollerShutterChannel channel) {
        return singletonList(createChannel(channel, ROLLER_SHUTTER_CHANNEL_ID, "Rollershutter"));
    }

    @Override
    public List<Channel> onDimmerChannel(final DimmerChannel channel) {
        return singletonList(createChannel(channel, DIMMER_CHANNEL_ID, "Dimmer"));
    }

    @Override
    public List<Channel> onRgbLightningChannel(final RgbLightningChannel channel) {
        return createLedChannels(channel);
    }

    @Override
    public List<Channel> onDimmerAndRgbLightningChannel(final DimmerAndRgbLightningChannel channel) {
        final List<Channel> ledChannels = createLedChannels(channel);
        final List<Channel> channels = new ArrayList<>(ledChannels);
        final Channel brightnessChannel = createChannel(DIMMER_CHANNEL_ID, "Dimmer", channel.getId() + "_brightness", "Brightness");
        channels.add(brightnessChannel);
        return channels;
    }

    @Override
    public List<Channel> onDepthChannel(final DepthChannel channel) {
        return singletonList(createChannel(channel, DECIMAL_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onDistanceChannel(final DistanceChannel channel) {
        return singletonList(createChannel(channel, DECIMAL_CHANNEL_ID, "Number"));
    }

    @Override
    public List<Channel> onElectricityMeterChannel(final ElectricityMeterChannel channel) {
        final List<Channel> channels = new ArrayList<>(asList(
                createChannel(TOTAL_COST_CHANNEL_ID, "String", channel, TOTAL_COST, "Total Cost"),
                createChannel(PRICE_PER_UNIT_CHANNEL_ID, "String", channel, PRICE_PER_UNIT, "Price per Unit")
        ));
        channels.addAll(createChannelsForPhase(channel));
        return unmodifiableList(channels);
    }

    private List<Channel> createChannelsForPhase(final ElectricityMeterChannel channel) {
        return asList(
                createChannel(NUMBER_OF_PHASES_ID, "Number", channel, PHASE_NUMBER, "Number of Phases"),
                createChannel(FREQUENCY_ID, "Number", channel, PHASE_FREQUENCY, "Frequency Phase"),
                createChannel(POWER_ACTIVE_ID, "Number", channel, PHASE_POWER_ACTIVE, "Power Active Phase"),
                createChannel(POWER_REACTIVE_ID, "Number", channel, PHASE_POWER_REACTIVE, "Power Reactive Phase"),
                createChannel(POWER_APPARENT_ID, "Number", channel, PHASE_POWER_APPARENT, "Power Apparent Phase"),
                createChannel(TOTAL_FORWARD_ACTIVE_ENERGY_ID, "Number", channel, PHASE_TOTAL_FORWARD_ACTIVE_ENERGY, "Total Forward Active Phase Energy"),
                createChannel(TOTAL_REVERSE_ACTIVE_ENERGY_ID, "Number", channel, PHASE_TOTAL_REVERSE_ACTIVE_ENERGY, "Total Reverse Active Phase Energy"),
                createChannel(TOTAL_FORWARD_REACTIVE_ENERGY_ID, "Number", channel, PHASE_TOTAL_FORWARD_REACTIVE_ENERGY, "Total Forward Reactive Energy"),
                createChannel(TOTAL_REVERSE_REACTIVE_ENERGY_ID, "Number", channel, PHASE_TOTAL_REVERSE_REACTIVE_ENERGY, "Total Reverse Reactive Energy")
        );
    }

    @Override
    public List<Channel> onDefault(final pl.grzeslowski.jsupla.api.channel.Channel channel) {
        logger.warn("Does not know type of this `{}` function", channel.getClass().getSimpleName());
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
            final String id,
            final String acceptedItemType,
            final pl.grzeslowski.jsupla.api.channel.Channel channel,
            final AdditionalChannelType additionalChannelType,
            final String caption) {
        return createChannel(id, acceptedItemType, Integer.toString(channel.getId()), additionalChannelType, caption);
    }

    private Channel createChannel(
            final String id,
            final String acceptedItemType,
            final String channelId,
            final AdditionalChannelType additionalChannelType,
            final String caption) {
        return createChannel(id, acceptedItemType, channelId + additionalChannelType.getSuffix(), caption);
    }

    private Channel createChannel(
            final pl.grzeslowski.jsupla.api.channel.Channel channel,
            final String id,
            final String acceptedItemType) {
        return createChannel(id, acceptedItemType, String.valueOf(channel.getId()), channel.getCaption());
    }

    private List<Channel> createSwitchChannel(pl.grzeslowski.jsupla.api.channel.Channel channel) {
        if (channel.isOutput()) {
            return singletonList(createChannel(channel, SWITCH_CHANNEL_ID, "Switch"));
        } else {
            return singletonList(createChannel(channel, SWITCH_CHANNEL_RO_ID, "Switch"));
        }
    }

    private List<Channel> createToggleGateChannel(pl.grzeslowski.jsupla.api.channel.Channel channel) {
        return singletonList(createChannel(channel, TOGGLE_GAT_CHANNEL_ID, "String"));
    }

    private List<Channel> createLedChannels(final pl.grzeslowski.jsupla.api.channel.Channel channel) {
        return asList(
                createChannel(channel, RGB_CHANNEL_ID, "Color"),
                createChannel(EXTRA_LIGHT_ACTIONS_CHANNEL_ID, "String", channel.getId() + EXTRA_LIGHT_ACTIONS.getSuffix(), "Extra Actions"));
    }
}
