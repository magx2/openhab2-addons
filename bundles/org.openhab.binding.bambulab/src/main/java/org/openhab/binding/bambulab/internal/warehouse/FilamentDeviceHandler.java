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
import org.openhab.binding.bambulab.internal.InitializationException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Mass;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.DEFAULT_FILAMENT_MASS_UNIT;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.FilamentChannel.WEIGHT_CHANNEL;
import static org.openhab.core.thing.ThingStatus.OFFLINE;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class FilamentDeviceHandler extends BaseThingHandler {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Logger logger = LoggerFactory.getLogger(FilamentDeviceHandler.class);
    private final WarehouseDb db;

    private @Nullable String color;
    private @Nullable FilamentType type;
    private @Nullable LocalDate dateOpened;
    private @Nullable QuantityType<Mass> initialWeight;
    private int nozzleTemperature;

    public FilamentDeviceHandler(Thing thing, WarehouseDb db) {
        super(thing);
        this.db = db;
        db.initializeFilament(thing.getUID());
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (InitializationException e) {
            updateStatus(OFFLINE, e.getThingStatusDetail(), e.getDescription());
        }
    }

    private void internalInitialize() throws InitializationException {
        updateConfiguration();
        refreshWeight(new ChannelUID(getThing().getUID(), WEIGHT_CHANNEL));
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateConfiguration() throws InitializationException {
        var thing = getThing();
        Configuration config = thing.getConfiguration();
        this.color = (String) config.get("color");
        var localType = parseType(config);
        this.type = localType;
        String dateOpenedString = (String) config.get("dateOpened");
        if (dateOpenedString != null && !dateOpenedString.isEmpty()) {
            this.dateOpened = LocalDate.parse(dateOpenedString, FORMATTER);
        } else {
            var dateOpened = LocalDate.now();
            this.dateOpened = dateOpened;
            var edit = editConfiguration();
            edit.put("dateOpened", FORMATTER.format(dateOpened));
            updateConfiguration(edit);
        }
        var iw = ((BigDecimal) config.get("initialWeight")).doubleValue();
        initialWeight = new QuantityType<>(iw, DEFAULT_FILAMENT_MASS_UNIT);
        BigDecimal configuredNozzleTemperature = (BigDecimal) config.get("nozzleTemperature");
        if (configuredNozzleTemperature != null && configuredNozzleTemperature.intValue() > 0) {
            this.nozzleTemperature = configuredNozzleTemperature.intValue();
        } else {
            Integer nt = localType.getNozzleTemperature();
            if (nt == null) {
                throw new InitializationException(ThingStatusDetail.CONFIGURATION_ERROR, "@text/thing-type.bambulab.filament.config.nozzleTemperatureMissing");
            }
            this.nozzleTemperature = nt;
        }
    }

    private static FilamentType parseType(Configuration config) {
        return FilamentType.valueOf((String) config.get("type"));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (WEIGHT_CHANNEL.equals(channelUID.getId())) {
            // weight channel
            switch (command) {
                case RefreshType rt -> refreshWeight(channelUID);
                case QuantityType<?> qt -> setWeight(channelUID, qt);
                case DecimalType dt -> setWeight(channelUID, new QuantityType<>(dt,DEFAULT_FILAMENT_MASS_UNIT ));
                default -> logger.debug("Unexpected value: {}", command);
            }
        }
    }

    private void refreshWeight(ChannelUID channelUID) {
        var weight = db.loadMass(channelUID).orElse(initialWeight);
        updateState(channelUID, requireNonNull(weight));
    }

    private void setWeight(ChannelUID channelUID, QuantityType<?> qt) {
        db.saveMass(channelUID,  qt);
        updateState(channelUID, qt);
    }
}
