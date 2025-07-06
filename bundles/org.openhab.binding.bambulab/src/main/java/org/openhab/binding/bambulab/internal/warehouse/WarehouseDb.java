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


import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Mass;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.synchronizedMap;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.*;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class WarehouseDb {
    public static final WarehouseDb INSTANCE = new WarehouseDb();
    private final Logger logger = LoggerFactory.getLogger(WarehouseDb.class.getName());
    private final Map<String, SynchronizedMonad> monads = synchronizedMap(new HashMap<>());
    private final Path bindingDir;
    private final Gson gson = new Gson();


    private WarehouseDb() {
        bindingDir = Path.of(System.getProperty("openhab.userdata"), BINDING_ID);
        logger.debug("Warehouse Database Path: {}", bindingDir);
        try {
            logger.debug("Creating dirs for {}", bindingDir);
            Files.createDirectories(bindingDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create dirs for " + bindingDir, e);
        }
    }

    public void initializeFilament(ThingUID thingUID) {
        var path = thingFilePath(thingUID);
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create file " + path, e);
        }
    }

    public Optional<QuantityType<Mass>> loadMass(ChannelUID channelUID) {
        var thingUID = channelUID.getThingUID();
        return monads.computeIfAbsent(thingUID.getId(), __ -> new SynchronizedMonad()) //
                .read(() ->//
                        buildFileForThingUid(thingUID)//
                                .map(Path::toFile)//
                                .map(this::newFileReader)//
                                .flatMap(file -> {
                                    logger.debug("Load mass for {}", channelUID);
                                    //noinspection OptionalOfNullableMisuse
                                    return Optional.ofNullable(gson.fromJson(file, FilamentDb.class));
                                }))//
                .map(FilamentDb::getWeight)//
                .map(weight -> new QuantityType<>(weight, DEFAULT_FILAMENT_MASS_UNIT));
    }

    public void saveMass(ChannelUID channelUID, QuantityType<?> qt) {
        var massType = qt.toUnit(DEFAULT_FILAMENT_MASS_UNIT);
        if (massType == null) {
            logger.warn("QuantityType<?> {} is not compatible with QuantityType<{}>!", qt, DEFAULT_FILAMENT_MASS_UNIT);
            return;
        }
        var mass = massType.doubleValue();
        if (mass < 0) {
            throw new IllegalArgumentException("Mass cannot be negative! Was " + mass);
        }
        var thingUID = channelUID.getThingUID();
        monads.computeIfAbsent(thingUID.getId(), __ -> new SynchronizedMonad()) //
                .write(() ->//
                        buildFileForThingUid(thingUID)//
                                .map(Path::toFile)//
                                .ifPresent(file -> {
                                    logger.debug("Save mass {} for {}", mass, channelUID);
                                    var filament = new FilamentDb(mass);
                                    var json = gson.toJson(filament, FilamentDb.class);
                                    writeToFile(file.toPath(), json);
                                }));
    }

    private void writeToFile(Path path, String value) {
        try {
            Files.writeString(path, value, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write to file " + path, e);
        }
    }

    private Reader newFileReader(File file) {
        try {
            return new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("Cannot read from file " + file, e);
        }
    }

    private Optional<Path> buildFileForThingUid(ThingUID thingUID) {
        var normalize = thingFilePath(thingUID);
        if (!Files.exists(normalize)) {
            return Optional.empty();
        }
        return Optional.of(normalize);
    }

    private Path thingFilePath(ThingUID thingUID) {
        return bindingDir.resolve(thingUID.getId() + ".json").normalize();
    }
}
