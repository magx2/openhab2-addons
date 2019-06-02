package org.openhab.binding.supla.internal.cloud.executors;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.supla.internal.cloud.api.ChannelsCloudApi;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.ChannelExecuteActionRequest;

import static org.mockito.Mockito.verify;
import static pl.grzeslowski.jsupla.api.generated.model.ChannelFunctionActionEnum.SET_RGBW_PARAMETERS;

@SuppressWarnings("WeakerAccess")
@ExtendWith(MockitoExtension.class)
@ExtendWith(RandomBeansExtension.class)
class SuplaLedCommandExecutorTest {
    @InjectMocks SuplaLedCommandExecutor executor;
    @Mock ChannelsCloudApi channelsApi;

    ChannelUID channelUID = new ChannelUID("a:b:c:d");

    @Test
    @DisplayName("should send update about change of brightness for dimmer")
    void dimmer(@Random int channelId) throws ApiException {

        // given
        final int brightnessValue = 55;
        final PercentType brightness = new PercentType(brightnessValue);
        executor.setLedState(channelUID, PercentType.ZERO);

        // when
        executor.changeBrightness(channelId, channelUID, brightness);

        // then
        final ChannelExecuteActionRequest expectedAction = new ChannelExecuteActionRequest()
                                                                   .action(SET_RGBW_PARAMETERS)
                                                                   .brightness(brightnessValue);
        verify(channelsApi).executeAction(expectedAction, channelId);
    }

    @Test
    @DisplayName("should send update about change of brightness for dimmer and rgb")
    void dimmerAndRgbChangeOfBrightness(@Random int channelId) throws ApiException {

        // given
        final int brightnessValue = 55;
        final PercentType brightness = new PercentType(brightnessValue);
        executor.setLedState(channelUID, PercentType.ZERO);
        executor.setLedState(channelUID, HSBType.BLUE);

        // when
        executor.changeBrightness(channelId, channelUID, brightness);

        // then
        final ChannelExecuteActionRequest expectedAction = new ChannelExecuteActionRequest()
                                                                   .action(SET_RGBW_PARAMETERS)
                                                                   .color("0x0000FF")
                                                                   .colorBrightness(100)
                                                                   .brightness(brightnessValue);
        verify(channelsApi).executeAction(expectedAction, channelId);
    }

    @Test
    @DisplayName("should send update about change of color for dimmer and rgb")
    void dimmerAndRgbChangeOfColor(@Random int channelId) throws ApiException {

        // given
        final int brightnessValue = 55;
        executor.setLedState(channelUID, new PercentType(brightnessValue));
        executor.setLedState(channelUID, HSBType.BLUE);

        // when
        executor.changeColor(channelId, channelUID, HSBType.RED);

        // then
        final ChannelExecuteActionRequest expectedAction = new ChannelExecuteActionRequest()
                                                                   .action(SET_RGBW_PARAMETERS)
                                                                   .color("0xFF0000")
                                                                   .colorBrightness(100)
                                                                   .brightness(brightnessValue);
        verify(channelsApi).executeAction(expectedAction, channelId);
    }
}
