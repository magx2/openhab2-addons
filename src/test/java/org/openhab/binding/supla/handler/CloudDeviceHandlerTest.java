package org.openhab.binding.supla.handler;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import io.swagger.client.model.ChannelExecuteActionRequest;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
import pl.grzeslowski.jsupla.api.Color;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.ControllingChannel;
import pl.grzeslowski.jsupla.api.channel.DimmerAndRgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.GateChannel;
import pl.grzeslowski.jsupla.api.channel.OnOffChannel;
import pl.grzeslowski.jsupla.api.channel.RgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.RollerShutterChannel;
import pl.grzeslowski.jsupla.api.channel.action.OpenCloseAction;
import pl.grzeslowski.jsupla.api.channel.action.ShutRevealAction;
import pl.grzeslowski.jsupla.api.channel.action.StopAction;
import pl.grzeslowski.jsupla.api.channel.action.ToggleAction;
import pl.grzeslowski.jsupla.api.channel.action.TurnOnOffAction;
import pl.grzeslowski.jsupla.api.channel.state.ColorAndBrightnessState;
import pl.grzeslowski.jsupla.api.channel.state.ColorState;
import pl.grzeslowski.jsupla.api.channel.state.OnOffState;
import pl.grzeslowski.jsupla.api.channel.state.Percentage;
import pl.grzeslowski.jsupla.api.channel.state.RollerShutterState;
import pl.grzeslowski.jsupla.api.device.Device;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;
import static org.apache.commons.lang3.reflect.FieldUtils.getField;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.OFF_LIGHT_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.OPEN_CLOSE_GATE_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.Commands.WHITE_LIGHT_COMMAND;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_DEVICE_CLOUD_ID;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.EXTRA_LIGHT_ACTIONS;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.LED_BRIGHTNESS;

@SuppressWarnings({"WeakerAccess", "unused"})
@ExtendWith({MockitoExtension.class, RandomBeansExtension.class})
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
    @Mock OnOffChannel lightChannel;
    @Random
    @Min(1) @Max(100) int lightChannelId;
    @Mock RgbLightningChannel rgbChannel;
    @Random
    @Min(1) @Max(100) int rgbChannelId;
    @Mock DimmerAndRgbLightningChannel dimmerAndRgbChannel;
    @Random
    @Min(1) @Max(100) int dimmerAndRgbChannelId;
    @Mock RollerShutterChannel rollerShutterChannel;
    @Random
    @Min(1) @Max(100) int rollerShutterChannelId;
    @Mock GateChannel gateChannel;
    @Random
    @Min(1) @Max(100) int gateChannelId;
    @Mock ControllingChannel garageDoorChannel;
    @Random
    @Min(1) @Max(100) int garageDoorChannelId;

    SortedSet<Channel> allChannels;

    @Random String oAuthToken;
    @Random long refreshInterval;
    @Random
    @Min(1) @Max(100) int cloudId;

    ThingUID thingUID = new ThingUID("supla", "1337");

    @Captor ArgumentCaptor<ChannelExecuteActionRequest> channelExecuteActionRequestCaptor;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        setUpChannels();
        setUpInternalInitialize();
    }

    void setUpChannels() {
        getAllFieldsList(CloudDeviceHandlerTest.class)
                .stream()
                .filter(field -> Channel.class.isAssignableFrom(field.getType()))
                .map(field -> Pair.with(field, (Channel) readField(field)))
                .map(pair -> Pair.with(getField(CloudDeviceHandlerTest.class, pair.getValue0().getName() + "Id", true), pair.getValue1()))
                .map(pair -> Pair.with((int) readField(pair.getValue0()), pair.getValue1()))
                .forEach(pair -> {
                    lenient().when(pair.getValue1().getId()).thenReturn(pair.getValue0());
                    lenient().when(pair.getValue1().compareTo(any())).thenReturn(-1);
                });
        allChannels = getAllFieldsList(CloudDeviceHandlerTest.class)
                              .stream()
                              .filter(field -> Channel.class.isAssignableFrom(field.getType()))
                              .map(this::readField)
                              .map(channel -> (Channel) channel)
                              .collect(Collectors.toCollection(TreeSet::new));
    }

    private Object readField(final Field field) {
        try {
            return FieldUtils.readDeclaredField(this, field.getName(), true);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    void setUpInternalInitialize() throws IllegalAccessException {
        given(thing.getBridgeUID()).willReturn(bridgeUid);
        given(bridge.getHandler()).willReturn(bridgeHandler);
        given(bridgeHandler.getOAuthToken()).willReturn(Optional.of(oAuthToken));
        given(bridgeHandler.getRefreshInterval()).willReturn(Optional.of(refreshInterval));
        given(thing.getConfiguration()).willReturn(configuration);
        given(configuration.get(SUPLA_DEVICE_CLOUD_ID)).willReturn(cloudId);
        given(channelsCloudApiFactory.newChannelsCloudApi(oAuthToken, refreshInterval, SECONDS)).willReturn(channelsCloudApi);
        given(ioDevicesCloudApiFactory.newIoDevicesCloudApi(oAuthToken, refreshInterval, SECONDS)).willReturn(ioDevicesCloudApi);
        given(ioDevicesCloudApi.getIoDevice(cloudId)).willReturn(device);
        given(device.isConnected()).willReturn(true);
        given(device.isEnabled()).willReturn(true);
        given(device.getChannels()).willReturn(allChannels);
        given(thing.getUID()).willReturn(thingUID);
        given(ledCommandExecutorFactory.newLedCommandExecutor(channelsCloudApi)).willReturn(ledCommandExecutor);
        lenient().when(channelsCloudApi.getChannel(anyInt())).thenAnswer(invocationOnMock -> {
            int channelId = invocationOnMock.getArgument(0);
            return allChannels.stream()
                           .filter(channel -> channel.getId() == channelId)
                           .findAny()
                           .orElseThrow(() -> new IllegalArgumentException("Cannot find channel with ID=" + channelId));
        });

        handler = new CloudDeviceHandler(thing, channelsCloudApiFactory, ioDevicesCloudApiFactory, ledCommandExecutorFactory);
        writeField(handler, "callback", callback, true);
        doAnswer(__ -> {
            writeField(handler, "thing", thing, true);
            return null;
        }).when(callback).thingUpdated(any());
        given(callback.getBridge(bridgeUid)).willReturn(bridge);
        handler.internalInitialize();
        verify(callback, times(2)).statusUpdated(thing, new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    @Test
    @DisplayName("should send request to Supla Cloud to turn light ON")
    void lightChannelOn() {
        // given
        final ChannelUID lightChannelUID = findLightChannelUID();

        // when
        handler.handleOnOffCommand(lightChannelUID, OnOffType.ON);

        // then
        verify(channelsCloudApi).executeAction(lightChannel, TurnOnOffAction.ON);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to turn light OFF")
    void lightChannelOff() throws Exception {

        // given
        final ChannelUID lightChannelUID = findLightChannelUID();

        // when
        handler.handleOnOffCommand(lightChannelUID, OnOffType.OFF);

        // then
        verify(channelsCloudApi).executeAction(lightChannel, TurnOnOffAction.OFF);
    }

    @DisplayName("should send request to Supla Cloud to open gate or garage door")
    @Test
    void gateChannelOn() {
        // given
        final ChannelUID channelUID = buildChannelUID(gateChannelId);

        // when
        handler.handleOnOffCommand(channelUID, OnOffType.ON);

        // then
        verify(channelsCloudApi).executeAction(gateChannel, OpenCloseAction.OPEN);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to close gate or garage door")
    void gateChannelOff() {
        // given
        final ChannelUID channelUID = buildChannelUID(gateChannelId);

        // when
        handler.handleOnOffCommand(channelUID, OFF);

        // then
        verify(channelsCloudApi).executeAction(gateChannel, OpenCloseAction.CLOSE);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to reveal roller shutter")
    void rollerShutterUp() {
        // given
        final ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleUpDownCommand(rollerShutterChannelUID, UpDownType.UP);

        // then
        verify(channelsCloudApi).executeAction(rollerShutterChannel, ShutRevealAction.reveal());
    }

    @Test
    @DisplayName("should send request to Supla Cloud to shut roller shutter")
    void rollerShutterDown() {
        // given
        final ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleUpDownCommand(rollerShutterChannelUID, UpDownType.DOWN);

        // then
        verify(channelsCloudApi).executeAction(rollerShutterChannel, ShutRevealAction.shut());
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
        verify(channelsCloudApi).executeAction(rollerShutterChannel, ShutRevealAction.reveal(percentage));
    }

    @ParameterizedTest(name = "should send request to LedExecutor to change color brightness for {0}")
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    void revealPartiallyRollerShutter(String idFieldName) throws Exception {
        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final Channel channel = (Channel) FieldUtils.readDeclaredField(this, idFieldName.replace("Id", ""), true);
        final ChannelUID channelUID = buildChannelUID(id);
        final PercentType command = new PercentType(33);

        // when
        handler.handlePercentCommand(channelUID, command);

        // then
        verify(ledCommandExecutor).changeColorBrightness(channel, command);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to change dimmer")
    void changeBrightness() {
        // given
        ChannelUID dimmerAndRgbChannelUID = findDimmerAndRgbChannelUID(LED_BRIGHTNESS);
        final PercentType command = new PercentType(33);

        // when
        handler.handlePercentCommand(dimmerAndRgbChannelUID, command);

        // then
        verify(ledCommandExecutor).changeBrightness(dimmerAndRgbChannel, command);
    }

    @Test
    @DisplayName("should send request to Supla Cloud to stop roller shutter")
    void stopRollerShutter() {
        // given
        ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleStopMoveTypeCommand(rollerShutterChannelUID, StopMoveType.STOP);

        // then
        verify(channelsCloudApi).executeAction(rollerShutterChannel, StopAction.STOP);
    }

    @Test
    @DisplayName("should do nothing on move command for roller shutter")
    void moveRollerShutter() {
        // given
        ChannelUID rollerShutterChannelUID = findRollerShutterChannelUID();

        // when
        handler.handleStopMoveTypeCommand(rollerShutterChannelUID, StopMoveType.MOVE);

        // then
        verify(channelsCloudApi, never()).executeAction(eq(rollerShutterChannel), any());
    }

    @ParameterizedTest(name = "show send request to Supla CLoud to OPEN/CLOSE {0}}")
    @ValueSource(strings = {"gateChannelId", "garageDoorChannelId"})
    void openCloseGateAndGarage(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final Channel channel = (Channel) FieldUtils.readDeclaredField(this, idFieldName.replace("Id", ""), true);
        final ChannelUID channelUID = buildChannelUID(id);

        // when
        handler.handleStringCommand(channelUID, new StringType(OPEN_CLOSE_GATE_COMMAND));

        // then
        verify(channelsCloudApi).executeAction(channel, ToggleAction.OPEN_CLOSE);
    }

    @ParameterizedTest(name = "should send request to LedExecutor to change color to white on channel {0}")
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    void setLightColorToWhite(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final Channel channel = (Channel) FieldUtils.readDeclaredField(this, idFieldName.replace("Id", ""), true);
        final ChannelUID channelUID = buildChannelUID(id, EXTRA_LIGHT_ACTIONS);
        final ChannelUID parentChannelUID = buildChannelUID(id);

        // when
        handler.handleStringCommand(channelUID, new StringType(WHITE_LIGHT_COMMAND));

        // then
        verify(ledCommandExecutor).changeColor(channel, HSBType.WHITE);
        verify(callback).stateUpdated(parentChannelUID, HSBType.WHITE);
    }

    @ParameterizedTest(name = "should send request to LedExecutor to change color to black on channel {0}")
    @ValueSource(strings = {"rgbChannelId", "dimmerAndRgbChannelId"})
    void turnOffRgbLights(String idFieldName) throws Exception {

        // given
        final int id = (int) FieldUtils.readDeclaredField(this, idFieldName, true);
        final Channel channel = (Channel) FieldUtils.readDeclaredField(this, idFieldName.replace("Id", ""), true);
        final ChannelUID channelUID = buildChannelUID(id, EXTRA_LIGHT_ACTIONS);
        final ChannelUID parentChannelUID = buildChannelUID(id);

        // when
        handler.handleStringCommand(channelUID, new StringType(OFF_LIGHT_COMMAND));

        // then
        verify(ledCommandExecutor).changeColor(channel, HSBType.BLACK);
        verify(callback).stateUpdated(parentChannelUID, HSBType.BLACK);
    }

    @Test
    @DisplayName("should refresh light and set ON")
    void refreshLightOn() {
        // given
        final ChannelUID channelUID = findLightChannelUID();
        given(lightChannel.findState()).willReturn(Optional.of(() -> OnOffState.OnOff.ON));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, ON);
    }

    @Test
    @DisplayName("should refresh light and set OFF")
    void refreshLightOff() {
        // given
        final ChannelUID channelUID = findLightChannelUID();
        given(lightChannel.findState()).willReturn(Optional.of(() -> OnOffState.OnOff.OFF));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, OFF);
    }

    @Test
    @DisplayName("should refresh rgbChannel")
    void refreshRgbChannel() {
        // given
        final ChannelUID channelUID = buildChannelUID(rgbChannelId);
        given(rgbChannel.findState()).willReturn(Optional.of(new ColorState() {
            @Override
            public Color.Rgb getRgb() {
                return new Color.Rgb(255, 0, 0);
            }

            @Override
            public Color.Hsv getHsv() {
                return getRgb().toHsv();
            }
        }));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, HSBType.RED);
    }

    @Test
    @DisplayName("should refresh dimmerAndRgbChannel")
    void refreshDimmerAndRgbChannel() {
        // given
        final ChannelUID channelUID = buildChannelUID(dimmerAndRgbChannelId);
        given(dimmerAndRgbChannel.findState()).willReturn(Optional.of(new ColorAndBrightnessState() {
            @Override
            public Percentage getBrightness() {
                return Percentage.MAX;
            }

            @Override
            public Color.Rgb getRgb() {
                return new Color.Rgb(255, 0, 0);
            }

            @Override
            public Color.Hsv getHsv() {
                return getRgb().toHsv();
            }
        }));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, HSBType.RED);
    }

    @Test
    @DisplayName("should refresh dimmer and RGB brightness channel")
    void dimmerAndRgbRefresh() {

        // given
        final ChannelUID channelUID = findDimmerAndRgbChannelUID(LED_BRIGHTNESS);
        final int brightness = 57;
        given(dimmerAndRgbChannel.findState()).willReturn(Optional.of(new ColorAndBrightnessState() {
            @Override
            public Percentage getBrightness() {
                return new Percentage(brightness);
            }

            @Override
            public Color.Rgb getRgb() {
                return new Color.Rgb(255, 0, 0);
            }

            @Override
            public Color.Hsv getHsv() {
                return getRgb().toHsv();
            }
        }));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verifyUpdateState(channelUID, new PercentType(brightness));
    }

    @Test
    @DisplayName("should not refresh dimmer and RGB brightness channel")
    void dimmerAndRgbNotRefresh() throws Exception {

        // given
        final ChannelUID channelUID = findDimmerAndRgbChannelUID(EXTRA_LIGHT_ACTIONS);
        given(dimmerAndRgbChannel.findState()).willReturn(Optional.of(new ColorAndBrightnessState() {
            @Override
            public Percentage getBrightness() {
                return new Percentage(33);
            }

            @Override
            public Color.Rgb getRgb() {
                return new Color.Rgb(255, 0, 0);
            }

            @Override
            public Color.Hsv getHsv() {
                return getRgb().toHsv();
            }
        }));

        // when
        handler.handleRefreshCommand(channelUID);

        // then
        verify(callback, never()).stateUpdated(eq(channelUID), any());
    }

    @Test
    @DisplayName("should refresh roller shutter")
    void rollerShutterRefresh() {
        // given
        final ChannelUID channelUID = findRollerShutterChannelUID();
        final int shut = 13;
        given(rollerShutterChannel.findState()).willReturn(Optional.of(new RollerShutterState() {

            @Override
            public OnOff getOnOffState() {
                return null;
            }

            @Override
            public boolean isCalibrating() {
                return false;
            }

            @Override
            public Percentage getShut() {
                return new Percentage(shut);
            }

            @Override
            public Percentage getOpen() {
                return getShut().invert();
            }
        }));

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
