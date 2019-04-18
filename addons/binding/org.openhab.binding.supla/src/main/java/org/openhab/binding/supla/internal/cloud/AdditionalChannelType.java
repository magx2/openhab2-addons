package org.openhab.binding.supla.internal.cloud;

public enum AdditionalChannelType {
    LED_BRIGHTNESS("_brightness");

    private final String suffix;

    AdditionalChannelType(final String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
