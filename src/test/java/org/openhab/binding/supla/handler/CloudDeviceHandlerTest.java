package org.openhab.binding.supla.handler;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.State;
import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.supla.internal.cloud.AdditionalChannelType;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApiFactory;
import org.openhab.binding.supla.internal.cloud.api.IoDevicesCloudApi;
import org.openhab.binding.supla.internal.cloud.api.IoDevicesCloudApiFactory;
import org.openhab.binding.supla.internal.cloud.executors.LedCommandExecutor;
import org.openhab.binding.supla.internal.cloud.executors.LedCommandExecutorFactory;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.Channel;
import pl.grzeslowski.jsupla.api.generated.model.ChannelExecuteActionRequest;
import pl.grzeslowski.jsupla.api.generated.model.ChannelFunction;
import pl.grzeslowski.jsupla.api.generated.model.ChannelState;
import pl.grzeslowski.jsupla.api.generated.model.ChannelType;
import pl.grzeslowski.jsupla.api.generated.model.Device;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;
import static org.apache.commons.lang3.reflect.FieldUtils.getField;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.OFF_LIGHT_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.OPEN_CLOSE_GATE_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.WHITE_LIGHT_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_DEVICE_CLOUD_ID;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.EXTRA_LIGHT_ACTIONS;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.LED_BRIGHTNESS;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.CLOSE;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.OPEN;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.OPEN_CLOSE;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.REVEAL;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.REVEAL_PARTIALLY;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.SHUT;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.STOP;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.TURN_OFF;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.TURN_ON;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.CONTROLLINGTHEGARAGEDOOR;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.CONTROLLINGTHEGATE;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.CONTROLLINGTHEROLLERSHUTTER;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.DIMMERANDRGBLIGHTING;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.LIGHTSWITCH;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.POWERSWITCH;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionEnumNames.RGBLIGHTING;

@SuppressWarnings({"WeakerAccess", "unused"})
@ExtendWith(MockitoExtension.class)
@ExtendWith(RandomBeansExtension.class)
class CloudDeviceHandlerTest {
    CloudDeviceHandler handler;
    @Mock Thing thing;
    @Mock Bridge bridge;
    @Mock ThingUID bridgeUid;
    @Mock LedCommandExecutorFactory ledCommandExecutorFactory;
    @Mock LedCommandExecutor ledCommandExecutor;
    @Mock ThingRegistry thingRegistry;
    @Mock CloudBridgeHandler bridgeHandler;
    @Mock Configuration configuration;
    @Mock ChannelsCloudApiFactory channelsCloudApiFactory;
    @Mock IoDevicesCloudApiFactory ioDevicesCloudApiFactory;
    @Mock ChannelsCloudApi channelsCloudApi;
    @Mock IoDevicesCloudApi ioDevicesCloudApi;
    @Mock Device device;
    @Mock ThingHandlerCallback callback;

    // Channels
    @Mock Channel lightChannel;
    @Random
    @Min(1) @Max(100) int lightChannelId;
    @Mock Channel powerSwitchChannel;
    @Random
    @Min(1) @Max(100) int powerSwitchChannelId;
    @Mock Channel rgbChannel;
    @Random
    @Min(1) @Max(100) int rgbChannelId;
    @Mock Channel dimmerAndRgbChannel;
    @Random
    @Min(1) @Max(100) int dimmerAndRgbChannelId;
    @Mock Channel rollerShutterChannel;
    @Random
    @Min(1) @Max(100) int rollerShutterChannelId;
    @Mock Channel gateChannel;
    @Random
    @Min(1) @Max(100) int gateChannelId;
    @Mock Channel garageDoorChannel;
    @Random
    @Min(1) @Max(100) int garageDoorChannelId;

    List<Channel> allChannels;

    @Random String oAuthToken;
    @Random
    @Min(1) @Max(100) int cloudId;

    ThingUID thingUID = new ThingUID("supla", "1337");

    @Captor ArgumentCaptor<ChannelExecuteActionRequest> channelExecuteActionRequestCaptor;

    @BeforeEach
    void setUp() throws IllegalAccessException, ApiException {
        setUpChannels();
        setUpInternalInitialize();
    }

    void setUpChannels() {
        allChannels = getAllFieldsList(CloudDeviceHandlerTest.class)
                              .stream()
                              .filter(field -> Channel.class.isAssignableFrom(field.getType()))
                              .map(this::readField)
                              .map(channel -> (Channel) channel)
                              .collect(Collectors.toList());
        allChannels.forEach(channel -> given(channel.isHidden()).willReturn(false));
        given(lightChannel.getFunction()).willReturn(new ChannelFunction().name(LIGHTSWITCH));
        given(powerSwitchChannel.getFunction()).willReturn(new ChannelFunction().name(POWERSWITCH));
        given(powerSwitchChannel.getType()).willReturn(new ChannelType().output(true));
        given(rgbChannel.getFunction()).willReturn(new ChannelFunction().name(RGBLIGHTING));
        given(dimmerAndRgbChannel.getFunction()).willReturn(new ChannelFunction().name(DIMMERANDRGBLIGHTING));
        given(rollerShutterChannel.getFunction()).willReturn(new ChannelFunction().name(CONTROLLINGTHEROLLERSHUTTER));
        given(gateChannel.getFunction()).willReturn(new ChannelFunction().name(CONTROLLINGTHEGATE));
        given(garageDoorChannel.getFunction()).willReturn(new ChannelFunction().name(CONTROLLINGTHEGARAGEDOOR));

        getAllFieldsList(CloudDeviceHandlerTest.class)
                .stream()
                .filter(field -> Channel.class.isAssignableFrom(field.getType()))
                .map(field -> Pair.with(field, (Channel) readField(field)))
                .map(pair -> Pair.with(getField(CloudDeviceHandlerTest.class, pair.getValue0().getName() + "Id", true), pair.getValue1()))
                .map(pair -> Pair.with((int) readField(pair.getValue0()), pair.getValue1()))
                .forEach(pair -> given(pair.getValue1().getId()).willReturn(pair.getValue0()));
    }

    private Object readField(final Field field) {
        try {
            return FieldUtils.readDeclaredField(this, field.getName(), true);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    void setUpInternalInitialize() throws ApiException, IllegalAccessException {
        given(thing.getBridgeUID()).willReturn(bridgeUid);
        given(thingRegistry.get(bridgeUid)).willReturn(bridge);
        given(bridge.getHandler()).willReturn(bridgeHandler);
        given(bridgeHandler.getOAuthToken()).willReturn(Optional.of(oAuthToken));
        given(thing.getConfiguration()).willReturn(configuration);
        given(configuration.get(SUPLA_DEVICE_CLOUD_ID)).willReturn(cloudId);
        given(channelsCloudApiFactory.newChannelsCloudApi(oAuthToken)).willReturn(channelsCloudApi);
        given(ioDevicesCloudApiFactory.newIoDevicesCloudApi(oAuthToken)).willReturn(ioDevicesCloudApi);
        given(ioDevicesCloudApi.getIoDevice(eq(cloudId), any())).willReturn(device);
        given(device.isConnected()).willReturn(true);
        given(device.isEnabled()).willReturn(true);
        given(device.getChannels()).willReturn(allChannels);
        given(thing.getUID()).willReturn(thingUID);
        given(ledCommandExecutorFactory.newLedCommandExecutor(channelsCloudApi)).willReturn(ledCommandExecutor);
        given(channelsCloudApi.getChannel(anyInt(), any())).willAnswer(invocationOnMock -> {
            int channelId = invocationOnMock.getArgument(0);
            return allChannels.stream()
                           .filter(channel -> channel.getId() == channelId)
                           .findAny()
                           .orElseThrow(IllegalArgumentException::new);
        });

        handler = new CloudDeviceHandler(thing, channelsCloudApiFactory, ioDevicesCloudApiFactory, ledCommandExecutorFactory);
        writeField(handler, "thingRegistry", thingRegistry, true);
        writeField(handler, "callback", callback, true);
        doAnswer(__ -> {
            writeField(handler, "thing", thing, true);
            return null;
        }).when(callback).thingUpdated(any());
        handler.internalInitialize();
        verify(callback).statusUpdated(thing, new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    @Test
    @DisplayName("should send request to Supla Cloud to turn light ON")
    void lightChannelOn() throws Exception {

        // given
        final ChannelUID lightChannelUID = findLightChannelUID();

        // when
        handler.handleOnOffCommand(lightChannelUID, OnOffType.ON);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(lightChannelId));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(TURN_ON);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to turn light OFF")
    void lightChannelOff() throws Exception {

        // given
        final ChannelUID lightChannelUID = findLightChannelUID();

        // when
        handler.handleOnOffCommand(lightChannelUID, OnOffType.OFF);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(lightChannelId));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(TURN_OFF);
    }

    @DisplayName("should send request to Supla Cloud to open gate or garage door")
    @ParameterizedTest
    @ValueSource(strings = {"gateChannelId", "garageDoorChannelId"})
    void gateChannelOn(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);

        // when
        handler.handleOnOffCommand(channelUID, OnOffType.ON);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(id));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(OPEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"gateChannelId", "garageDoorChannelId"})
    @DisplayName("should send request to Supla Cloud to close gate or garage door")
    void gateChannelOff(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);

        // when
        handler.handleOnOffCommand(channelUID, OnOffType.OFF);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(id));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(CLOSE);
    }

    @DisplayName("should send request to Supla Cloud to open gate or garage door")
    @ParameterizedTest
    @ValueSource(strings = {"gateChannelId", "garageDoorChannelId"})
    void gateChannelOpen(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);

        // when
        handler.handleOpenClosedCommand(channelUID, OpenClosedType.OPEN);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(id));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(OPEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"gateChannelId", "garageDoorChannelId"})
    @DisplayName("should send request to Supla Cloud to close gate or garage door")
    void gateChannelClose(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);

        // when
        handler.handleOpenClosedCommand(channelUID, OpenClosedType.CLOSED);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(id));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(CLOSE);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to reveal roller shutter")
    void rollerShutterUp() throws Exception {

        // given
        final ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleUpDownCommand(rollerShutterChannelUID, UpDownType.UP);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(rollerShutterChannelId));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(REVEAL);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to shut roller shutter")
    void rollerShutterDown() throws Exception {

        // given
        final ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleUpDownCommand(rollerShutterChannelUID, UpDownType.DOWN);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(rollerShutterChannelId));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(SHUT);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to reveal partially roller shutter")
    void revealPartiallyRollerShutter() throws Exception {

        // given
        final ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();
        final int percentage = 33;

        // when
        handler.handlePercentCommand(rollerShutterChannelUID, new PercentType(percentage));

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(rollerShutterChannelId));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(REVEAL_PARTIALLY);
        assertThat(value.getPercentage()).isEqualTo(percentage);
    }

    @ParameterizedTest
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    @DisplayName("should send request to LedExecutor to change color brightness")
    void revealPartiallyRollerShutter(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);
        final PercentType command = new PercentType(33);

        // when
        handler.handlePercentCommand(channelUID, command);

        // then
        verify(ledCommandExecutor).changeColorBrightness(id, command);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to reveal partially roller shutter")
    void changeBrightness() throws Exception {

        // given
        ChannelUID dimmerAndRgbChannelUID = findDimmerAndRgbChannelUID(LED_BRIGHTNESS);
        final PercentType command = new PercentType(33);

        // when
        handler.handlePercentCommand(dimmerAndRgbChannelUID, command);

        // then
        verify(ledCommandExecutor).changeBrightness(dimmerAndRgbChannelId, command);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to stop roller shutter")
    void stopRollerShutter() throws Exception {

        // given
        ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleStopMoveTypeCommand(rollerShutterChannelUID, StopMoveType.STOP);

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(rollerShutterChannelId));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(STOP);
    }

    @Test
    @DisplayName("should do nothing on move command for roller shutter")
    void moveRollerShutter() throws Exception {

        // given
        ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleStopMoveTypeCommand(rollerShutterChannelUID, StopMoveType.MOVE);

        // then
        verify(channelsCloudApi, times(0)).executeAction(channelExecuteActionRequestCaptor.capture(), eq(rollerShutterChannelId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gateChannelId", "garageDoorChannelId"})
    @DisplayName("show send request to Supla CLoud to OPEN/CLOSE gate or garage door")
    void openCloseGateAndGarage(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);

        // when
        handler.handleStringCommand(channelUID, new StringType(OPEN_CLOSE_GATE_COMMAND));

        // then
        verify(channelsCloudApi).executeAction(channelExecuteActionRequestCaptor.capture(), eq(id));
        ChannelExecuteActionRequest value = channelExecuteActionRequestCaptor.getValue();
        assertThat(value.getAction()).isEqualTo(OPEN_CLOSE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    @DisplayName("should send request to LedExecutor to change color to white")
    void setLightColorToWhite(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id, EXTRA_LIGHT_ACTIONS);
        final ChannelUID parentChannelUID = buildChannelUID(id);

        // when
        handler.handleStringCommand(channelUID, new StringType(WHITE_LIGHT_COMMAND));

        // then
        verify(ledCommandExecutor).changeColor(id, HSBType.WHITE);
        verify(callback).stateUpdated(parentChannelUID, HSBType.WHITE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    @DisplayName("should send request to LedExecutor to change color to black")
    void turnOffRgbLights(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id, EXTRA_LIGHT_ACTIONS);
        final ChannelUID parentChannelUID = buildChannelUID(id);

        // when
        handler.handleStringCommand(channelUID, new StringType(OFF_LIGHT_COMMAND));

        // then
        verify(ledCommandExecutor).changeColor(id, HSBType.BLACK);
        verify(callback).stateUpdated(parentChannelUID, HSBType.BLACK);
    }

    @ParameterizedTest
    @ValueSource(strings = {"lightChannelId", "powerSwitchChannelId"})
    @DisplayName("should refresh light and set ON")
    void refreshLightOn(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);
        final Channel channel = channelsCloudApi.getChannel(id, asList("supportedFunctions", "state"));
        given(channel.getState()).willReturn(new ChannelState().setOn(true));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, ON);
    }

    @ParameterizedTest
    @ValueSource(strings = {"lightChannelId", "powerSwitchChannelId"})
    @DisplayName("should refresh light and set OFF")
    void refreshLightOff(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);
        final Channel channel = channelsCloudApi.getChannel(id, asList("supportedFunctions", "state"));
        given(channel.getState()).willReturn(new ChannelState().setOn(false));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, OFF);
    }

    @ParameterizedTest
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    @DisplayName("should refresh RGB lights")
    @ExtendWith(RandomBeansExtension.class)
    void refreshRgb(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final ChannelUID channelUID = buildChannelUID(id);
        final Channel channel = channelsCloudApi.getChannel(id, asList("supportedFunctions", "state"));

        given(channel.getState()).willReturn(new ChannelState().setColor("0xFF0000").setColorBrightness(100));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, HSBType.RED);
    }

    @Test
    @DisplayName("should refresh dimmer and RGB brightness channel")
    void dimmerAndRgbRefresh() throws Exception {

        // given
        final ChannelUID channelUID = findDimmerAndRgbChannelUID(LED_BRIGHTNESS);
        final int brightness = 57;
        given(dimmerAndRgbChannel.getState()).willReturn(new ChannelState().setBrightness(brightness));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, new PercentType(brightness));
    }

    @Test
    @DisplayName("should refresh dimmer and RGB brightness channel")
    void dimmerAndRgbNotRefresh() throws Exception {

        // given
        final ChannelUID channelUID = findDimmerAndRgbChannelUID(EXTRA_LIGHT_ACTIONS);
        given(dimmerAndRgbChannel.getState()).willReturn(new ChannelState().setBrightness(13));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verify(callback, times(0)).stateUpdated(eq(channelUID), any());
    }

    @Test
    @DisplayName("should refresh roller shutter")
    void reollerShutterRefresh() throws Exception {

        // given
        final ChannelUID channelUID = findRollerShutterChannelUID();
        final int shut = 13;
        given(rollerShutterChannel.getState()).willReturn(new ChannelState().setShut(shut));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, new PercentType(shut));
    }

    ChannelUID buildChannelUID(int id) {
        return new ChannelUID(thingUID, valueOf(id));
    }

    @SuppressWarnings("SameParameterValue")
    ChannelUID buildChannelUID(int id, AdditionalChannelType channelType) {
        return new ChannelUID(thingUID, id + channelType.getSuffix());
    }

    ChannelUID findLightChannelUID() {
        return buildChannelUID(lightChannelId);
    }

    ChannelUID findRollerShutterChannelUID() {
        return buildChannelUID(rollerShutterChannelId);
    }

    @SuppressWarnings("SameParameterValue")
    ChannelUID findDimmerAndRgbChannelUID(AdditionalChannelType channelType) {
        return new ChannelUID(thingUID, dimmerAndRgbChannelId + channelType.getSuffix());
    }

    void verifyUpdateState(ChannelUID channelUID, State state) {
        verify(callback).stateUpdated(channelUID, state);
    }
}
