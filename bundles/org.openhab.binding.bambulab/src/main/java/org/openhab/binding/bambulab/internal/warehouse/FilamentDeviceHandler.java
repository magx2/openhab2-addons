/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bambulab.internal.warehouse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class FilamentDeviceHandler extends BaseThingHandler {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private @Nullable String color;
    private @Nullable FilamentType type;
    private @Nullable LocalDate dateOpened;
    private @Nullable String photo;
    private @Nullable Integer nozzleTemperature;

    public FilamentDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateConfiguration();
        updateState(new ChannelUID(getThing().getUID(), "weight"), UnDefType.UNDEF);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateConfiguration() {
        var thing = getThing();
        Configuration config = thing.getConfiguration();
        this.color = (String) config.get("color");
        this.type = parseType(config);
        String dateOpenedString = (String) config.get("dateOpened");
        if (dateOpenedString != null && !dateOpenedString.isEmpty()) {
            this.dateOpened = LocalDate.parse(dateOpenedString, FORMATTER);
        } else {
            var dateOpened = LocalDate.now();
            this.dateOpened = dateOpened;
            var edit = editConfiguration();
            edit.put("dateOpened",  FORMATTER.format(dateOpened));
            updateConfiguration(edit);
        }
        photo = (String) config.get("photo");
        Integer configuredNozzleTemperature = (Integer) config.get("nozzleTemperature");
        if (configuredNozzleTemperature != null) {
            this.nozzleTemperature = configuredNozzleTemperature;
        } else if (this.type != null) {
            this.nozzleTemperature = this.type.getNozzleTemperature();
        } else {
            this.nozzleTemperature = null;
        }
    }

    private static FilamentType parseType(Configuration config) {
        return FilamentType.valueOf((String) config.get("type"));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, UnDefType.UNDEF);
        } else if (channelUID.getId().equals("weight") && command instanceof DecimalType) {
            updateState(channelUID, (State) command);
        }
    }
}
