/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla;

import com.google.common.collect.ImmutableSet;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Set;

/**
 * The {@link SuplaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Grzeslowski - Initial contribution
 */
public class SuplaBindingConstants {

    public static final String BINDING_ID = "supla";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SUPLA_DEVICE_TYPE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID SUPLA_SERVER_TYPE = new ThingTypeUID(BINDING_ID, "server-bridge");
    public static final ThingTypeUID SUPLA_CLOUD_SERVER_TYPE = new ThingTypeUID(BINDING_ID, "supla-cloud-bridge");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            SUPLA_DEVICE_TYPE,
            SUPLA_SERVER_TYPE,
            SUPLA_CLOUD_SERVER_TYPE);
    
    // supla device and cloud-device
    public static final String SUPLA_DEVICE_GUID = "supla-device-guid";
    public static final String SUPLA_DEVICE_CLOUD_ID = "cloud-id";
    public static final String THREAD_POOL_NAME = "supla-cloud-thread-pool";

    // SuplaServer constants
    public static final int DEVICE_TIMEOUT_SEC = 10;
    public static final int DEFAULT_PORT = 2016;
    public static final String CONFIG_SERVER_ACCESS_ID = "serverAccessId";
    public static final String CONFIG_SERVER_ACCESS_ID_PASSWORD = "serverAccessIdPassword";
    public static final String CONFIG_EMAIL = "email";
    public static final String CONFIG_AUTH_KEY = "authKey";
    public static final String CONFIG_PORT = "port";
    public static final String CONNECTED_DEVICES_CHANNEL_ID = "server-devices";

    // CloudBridgeHandler
    public static final String O_AUTH_TOKEN = "oAuthToken";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String ADDRESS_CHANNEL_ID = "address";
    public static final String API_VERSION_CHANNEL_ID = "api-version";
    public static final String CLOUD_VERSION_CHANNEL_ID = "cloud-version";
    public static final String API_LIMIT_ID = "api-limit";
    public static final String API_REMAINING_LIMIT_ID = "api-remaining-limit";
    public static final String API_RESET_DATE_ID = "api-reset-date";
    public static final String API_LAST_UPDATE_DATE_ID = "api-last-update-date";
    public static final String REFRESH_CHANNEL_ID = "refresh";

    public static class Channels {
        public static final String LIGHT_CHANNEL_ID = "light-channel";
        public static final String SWITCH_CHANNEL_ID = "switch-channel";
        public static final String SWITCH_CHANNEL_RO_ID = "switch-channel-ro";
        public static final String DECIMAL_CHANNEL_ID = "decimal-channel";
        public static final String RGB_CHANNEL_ID = "rgb-channel";
        public static final String ROLLER_SHUTTER_CHANNEL_ID = "roller-shutter-channel";
        public static final String TEMPERATURE_CHANNEL_ID = "temperature-channel";
        public static final String HUMIDITY_CHANNEL_ID = "humidity-channel";
        public static final String DIMMER_CHANNEL_ID = "dimmer-channel";
        public static final String TOGGLE_GAT_CHANNEL_ID = "toggle-gate-channel";
        public static final String EXTRA_LIGHT_ACTIONS_CHANNEL_ID = "extra-light-actions-channel";
        public static final String TOTAL_COST_CHANNEL_ID = "total-cost-channel";
        public static final String PRICE_PER_UNIT_CHANNEL_ID = "price-per-unit-channel";
        public static final String UNKNOWN_CHANNEL_ID = "unknown-channel";

        public static class Phase {
            public static final String NUMBER_OF_PHASES_ID = "number-of-phases-channel";
            public static final String FREQUENCY_ID = "frequency-phase-channel";
            public static final String POWER_ACTIVE_ID = "power-active-phase-channel";
            public static final String POWER_REACTIVE_ID = "power-reactive-phase-channel";
            public static final String POWER_APPARENT_ID = "power-apparent-phase-channel";
            public static final String TOTAL_FORWARD_ACTIVE_ENERGY_ID = "total-forward-active-energy-phase-channel";
            public static final String TOTAL_REVERSE_ACTIVE_ENERGY_ID = "total-reverse-active-energy-phase-channel";
            public static final String TOTAL_FORWARD_REACTIVE_ENERGY_ID = "total-forward-reactive-energy-phase-channel";
            public static final String TOTAL_REVERSE_REACTIVE_ENERGY_ID = "total-reverse-reactive-energy-phase-channel";
        }
    }

    public static class Commands {
        public static final String OPEN_CLOSE_GATE_COMMAND = "open-close";
        public static final String WHITE_LIGHT_COMMAND = "light-white";
        public static final String OFF_LIGHT_COMMAND = "light-off";
    }
}
