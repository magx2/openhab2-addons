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

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public enum FilamentType {
    PLA(200),
    PETG(220),
    ABS(240),
    ASA(250),
    Nylon(260),
    PC(260),
    TPU(210),
    PVA(200),
    HIPS(230),
    Other(null);

    @Nullable private final Integer nozzleTemperature;

    FilamentType(@Nullable Integer nozzleTemperature) {
        this.nozzleTemperature = nozzleTemperature;
    }

    @Nullable
    public Integer getNozzleTemperature() {
        return nozzleTemperature;
    }
}
