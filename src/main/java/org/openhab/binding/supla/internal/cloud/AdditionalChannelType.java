package org.openhab.binding.supla.internal.cloud;

public enum AdditionalChannelType {
    LED_BRIGHTNESS("_brightness"),
    EXTRA_LIGHT_ACTIONS("_extra_light_actions"),
    TEMPERATURE("_temp"),
    HUMIDITY("_humidity"),
    TOTAL_COST("_total_cost"),
    PRICE_PER_UNIT("_price_per_unit"),
    // Phases
    PHASE_NUMBER("_phase_number"),
    PHASE_FREQUENCY("_phase_frequency"),
    PHASE_POWER_ACTIVE("_phase_power_active"),
    PHASE_POWER_REACTIVE("_phase_power_reactive"),
    PHASE_POWER_APPARENT("_phase_power_apparent"),
    PHASE_TOTAL_FORWARD_ACTIVE_ENERGY("_phase_tfae"),
    PHASE_TOTAL_REVERSE_ACTIVE_ENERGY("_phase_trae"),
    PHASE_TOTAL_FORWARD_REACTIVE_ENERGY("_phase_tfre"),
    PHASE_TOTAL_REVERSE_REACTIVE_ENERGY("_phase_trre")
    // end phases
    ;

    private final String suffix;

    AdditionalChannelType(final String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
