/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class ItemTestHelper {

    public static NumberItem createNumberItem(String name, int value) {
        NumberItem numberItem = new NumberItem(name);
        numberItem.setState(new DecimalType(value));
        return numberItem;
    }
}
