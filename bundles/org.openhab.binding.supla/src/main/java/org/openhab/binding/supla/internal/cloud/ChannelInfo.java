package org.openhab.binding.supla.internal.cloud;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

public final class ChannelInfo {
    private final int channelId;
    @Nullable private final AdditionalChannelType additionalChannelType;

    public ChannelInfo(final int channelId, final @Nullable AdditionalChannelType additionalChannelType) {
        this.channelId = channelId;
        this.additionalChannelType = additionalChannelType;
    }

    public int getChannelId() {
        return channelId;
    }

    public AdditionalChannelType getAdditionalChannelType() {
        return additionalChannelType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChannelInfo that = (ChannelInfo) o;
        return channelId == that.channelId &&
                       additionalChannelType == that.additionalChannelType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, additionalChannelType);
    }

    @Override
    public String toString() {
        return "ChannelInfo{" +
                       "channelId=" + channelId +
                       ", additionalChannelType=" + additionalChannelType +
                       '}';
    }
}
