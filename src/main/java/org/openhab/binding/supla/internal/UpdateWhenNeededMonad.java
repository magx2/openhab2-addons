package org.openhab.binding.supla.internal;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class UpdateWhenNeededMonad<T> {
    private final Object lock = new Object();
    private final long cacheEvictionTimeInMilliseconds;
    private final Supplier<T> instanceSupplier;
    private T instance;
    private long lastUpdateTime;

    public UpdateWhenNeededMonad(final Supplier<T> instanceSupplier,
                                 final long cacheEvictionTime,
                                 final TimeUnit timeUnit) {
        this.instanceSupplier = requireNonNull(instanceSupplier, "instanceSupplier");
        if (cacheEvictionTime <= 0) {
            throw new IllegalArgumentException(
                    "cacheEvictionTime cannot be smaller or equal 0! Was " + cacheEvictionTime);
        }
        cacheEvictionTimeInMilliseconds = timeUnit.toMillis(cacheEvictionTime);
    }

    public T get() {
        synchronized (lock) {
            if (findActualTime() >= lastUpdateTime + cacheEvictionTimeInMilliseconds) {
                instance = instanceSupplier.get();
                lastUpdateTime = findActualTime();
            }
        }
        return instance;
    }

    private long findActualTime() {
        return new Date().getTime();
    }
}
