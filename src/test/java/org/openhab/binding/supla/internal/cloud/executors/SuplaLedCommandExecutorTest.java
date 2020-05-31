package org.openhab.binding.supla.internal.cloud.executors;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;
import pl.grzeslowski.jsupla.api.Color;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.DimmerAndRgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.RgbLightningChannel;
import pl.grzeslowski.jsupla.api.channel.action.Action;
import pl.grzeslowski.jsupla.api.channel.action.SetBrightnessAction;
import pl.grzeslowski.jsupla.api.channel.action.SetBrightnessAndColor;
import pl.grzeslowski.jsupla.api.channel.action.SetColorAction;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("WeakerAccess")
@ExtendWith(MockitoExtension.class)
@ExtendWith(RandomBeansExtension.class)
class SuplaLedCommandExecutorTest {
    @InjectMocks SuplaLedCommandExecutor executor;
    @Mock ChannelsCloudApi channelsApi;

    @Random int channelId;

    @Test
    @DisplayName("should send update about change of brightness for dimmer")
    void dimmer(@Random int channelId) {

        // given
        final int brightnessValue = 55;
        final PercentType brightness = new PercentType(brightnessValue);
        final Channel channel = mock(Channel.class);
        given(channel.getId()).willReturn(channelId);

        executor.setLedState(channel, PercentType.ZERO);
        final Action expectedAction = new SetBrightnessAction(brightnessValue);

        // when
        executor.changeBrightness(channel, brightness);

        // then
        verify(channelsApi).executeAction(channel, expectedAction);
    }

    @Test
    @DisplayName("should send update about change of brightness for dimmer and rgb")
    void dimmerAndRgbChangeOfBrightness() {

        // given
        final int brightnessValue = 55;
        final PercentType brightness = new PercentType(brightnessValue);
        final Channel channel = mock(DimmerAndRgbLightningChannel.class);
        given(channel.getId()).willReturn(channelId);

        executor.setLedState(channel, PercentType.ZERO);
        executor.setLedState(channel, HSBType.BLUE);

        final Action expectedAction = new SetBrightnessAndColor(100, new Color.Rgb(0, 0, 255));

        // when
        executor.changeBrightness(channel, brightness);

        // then
        verify(channelsApi).executeAction(channel, expectedAction);
    }

    @Test
    @DisplayName("should send update about change of color for dimmer and rgb")
    void dimmerAndRgbChangeOfColor() {

        // given
        final int brightnessValue = 55;
        final Channel channel = mock(DimmerAndRgbLightningChannel.class);

        given(channel.getId()).willReturn(channelId);
        executor.setLedState(channel, new PercentType(brightnessValue));
        executor.setLedState(channel, HSBType.BLUE);

        final Action expectedAction = new SetBrightnessAndColor(100, new Color.Rgb(255, 0, 0));

        // when
        executor.changeColor(channel, HSBType.RED);

        // then
        verify(channelsApi).executeAction(channel, expectedAction);
    }

    @Test
    @DisplayName("should send update about change of brightness for rgb")
    void rgbChangeOfBrightness() {

        // given
        final int brightnessValue = 55;
        final PercentType brightness = new PercentType(brightnessValue);
        final Channel channel = mock(RgbLightningChannel.class);
        given(channel.getId()).willReturn(channelId);

        executor.setLedState(channel, PercentType.ZERO);
        executor.setLedState(channel, HSBType.BLUE);

        final Action expectedAction = SetColorAction.setRgb(new Color.Rgb(0, 0, 255));

        // when
        executor.changeBrightness(channel, brightness);

        // then
        verify(channelsApi).executeAction(channel, expectedAction);
    }

    @Test
    @DisplayName("should send update about change of color for rgb")
    void rgbChangeOfColor() {

        // given
        final int brightnessValue = 55;
        final Channel channel = mock(RgbLightningChannel.class);

        given(channel.getId()).willReturn(channelId);
        executor.setLedState(channel, new PercentType(brightnessValue));
        executor.setLedState(channel, HSBType.BLUE);

        final Action expectedAction = SetColorAction.setRgb(new Color.Rgb(255, 0, 0));

        // when
        executor.changeColor(channel, HSBType.RED);

        // then
        verify(channelsApi).executeAction(channel, expectedAction);
    }
}
