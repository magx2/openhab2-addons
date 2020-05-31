package org.openhab.binding.supla.internal.cloud.functionswitch;

import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.supla.internal.cloud.ChannelInfo;
import org.openhab.binding.supla.internal.cloud.ChannelInfoParser;
import org.openhab.binding.supla.internal.cloud.executors.LedCommandExecutor;
import pl.grzeslowski.jsupla.api.channel.TemperatureAndHumidityChannel;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.supla.internal.cloud.AdditionalChannelType.EXTRA_LIGHT_ACTIONS;

@ExtendWith(MockitoExtension.class)
@ExtendWith(RandomBeansExtension.class)
class FindStateFunctionSwitchTest {
    @InjectMocks FindStateFunctionSwitch functionSwitch;

    @Mock LedCommandExecutor ledCommandExecutor;
    @Mock ChannelUID channelUID;
    @Mock ChannelInfoParser channelInfoParser;

    @Test
    @DisplayName("should return throw nullPointerException if additional type is null")
    void onHumidityAndTemperatureNullAdditionalType() {
        // given
        final ChannelInfo channelInfo = new ChannelInfo(1, null);
        given(channelInfoParser.parse(channelUID)).willReturn(channelInfo);

        // when
        final Executable state = () -> functionSwitch.onTemperatureAndHumidityChannel(mock(TemperatureAndHumidityChannel.class));

        // then
        assertThrows(NullPointerException.class, state);
    }

    @Test
    @DisplayName("should return throw IllegalStateException if additional type is wrong")
    void onHumidityAndTemperatureWrongAdditionalType() {
        // given
        final ChannelInfo channelInfo = new ChannelInfo(1, EXTRA_LIGHT_ACTIONS);
        given(channelInfoParser.parse(channelUID)).willReturn(channelInfo);

        // when
        final Executable state = () -> functionSwitch.onTemperatureAndHumidityChannel(mock(TemperatureAndHumidityChannel.class));

        // then
        assertThrows(IllegalStateException.class, state);
    }
}
