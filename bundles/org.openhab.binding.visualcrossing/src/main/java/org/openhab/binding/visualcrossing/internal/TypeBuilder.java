/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.visualcrossing.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import java.util.Collection;
import java.util.function.Function;

import static javax.measure.MetricPrefix.MEGA;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;
import static org.openhab.core.types.UnDefType.NULL;
/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class TypeBuilder {
    private static State newStringType(@Nullable String string) {
        return string != null ? new StringType(string) : NULL;
    }

    public static <T> State newStringType(@Nullable T obj, Function<T, @Nullable String> function) {
        if (obj == null) {
            return NULL;
        }
        return newStringType(function.apply(obj));
    }

    private static State newStringType(@Nullable Collection<String> strings) {
        return strings != null ? new StringType(String.join(",", strings)) : NULL;
    }

    public static <T> State newStringCollectionType(@Nullable T obj,
            Function<T, @Nullable Collection<String>> function) {
        if (obj == null) {
            return NULL;
        }
        return newStringType(function.apply(obj));
    }

    private static State newDecimalType(@Nullable Number number) {
        return number != null ? new DecimalType(number) : NULL;
    }

    public static <T> State newDecimalType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newDecimalType(function.apply(obj));
    }

    private static State newTemperatureType(@Nullable Number temp) {
        return temp != null ? new QuantityType<>(temp, CELSIUS) : NULL;
    }

    public static <T> State newTemperatureType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newTemperatureType(function.apply(obj));
    }

    private static State newHumidityType(@Nullable Number humidity) {
        return humidity != null ? new DecimalType(humidity) : NULL;
    }

    public static <T> State newHumidityType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newHumidityType(function.apply(obj));
    }

    private static State newMilliLengthType(@Nullable Number length) {
        return length != null ? new QuantityType<>(length, MILLI(METRE)) : NULL;
    }

    public static <T> State newMilliLengthType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newMilliLengthType(function.apply(obj));
    }

    private static State newCentiLengthType(@Nullable Number length) {
        return length != null ? new QuantityType<>(length, CENTI(METRE)) : NULL;
    }

    public static <T> State newCentiLengthType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newCentiLengthType(function.apply(obj));
    }

    private static State newSpeedType(@Nullable Number speed) {
        return speed != null ? new QuantityType<>(speed, KILOMETRE_PER_HOUR) : NULL;
    }

    public static <T> State newSpeedType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newSpeedType(function.apply(obj));
    }

    private static State newAngleType(@Nullable Number angle) {
        return angle != null ? new QuantityType<>(angle, DEGREE_ANGLE) : NULL;
    }

    public static <T> State newAngleType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newAngleType(function.apply(obj));
    }

    private static State newMilliPressureType(@Nullable Number pressure) {
        return pressure != null ? new QuantityType<>(pressure, MILLI(BAR)) : NULL;
    }

    public static <T> State newMilliPressureType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newMilliPressureType(function.apply(obj));
    }

    private static State newKiloMeterType(@Nullable Number km) {
        return km != null ? new QuantityType<>(km, KILO(METRE)) : NULL;
    }

    public static <T> State newKiloMeterType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newKiloMeterType(function.apply(obj));
    }

    private static State newSolarRadiationType(@Nullable Number solarRadiation) {
        return solarRadiation != null ? new QuantityType<>(solarRadiation, WATT.divide(METRE.multiply(METRE))) : NULL;
    }

    public static <T> State newSolarRadiationType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newSolarRadiationType(function.apply(obj));
    }

    private static State newSolarEnergyType(@Nullable Number solarEnergy) {
        return solarEnergy != null ? new QuantityType<>(solarEnergy, MEGA(JOULE.divide(METRE.multiply(METRE)))) : NULL;
    }

    public static <T> State newSolarEnergyType(@Nullable T obj, Function<T, @Nullable Number> function) {
        if (obj == null) {
            return NULL;
        }
        return newSolarEnergyType(function.apply(obj));
    }
}
