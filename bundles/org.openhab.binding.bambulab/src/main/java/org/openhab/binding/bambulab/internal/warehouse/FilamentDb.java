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

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class FilamentDb {
    private double weight;

    public FilamentDb() {
    }

    public FilamentDb(double weight) {
        this();
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if(weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "FilamentDb{" +
                "weight=" + weight +
                '}';
    }
}
