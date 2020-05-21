package org.openhab.binding.supla.internal.cloud.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.device.Device;

import java.util.SortedSet;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnstableApiUsage")
final class GuavaIoDevicesCloudApi implements IoDevicesCloudApi {
    private final LoadingCache<Singleton, SortedSet<Device>> getIoDevicesCache;

    private enum Singleton {
        KEY
    }

    GuavaIoDevicesCloudApi(final IoDevicesCloudApi ioDevicesCloudApi) {
        getIoDevicesCache = CacheBuilder.newBuilder()
                                    .expireAfterWrite(GuavaCache.cacheEvictTime, GuavaCache.cacheEvictUnit)
                                    .build(new CacheLoader<Singleton, SortedSet<Device>>() {
                                        @Override
                                        public SortedSet<Device> load(final Singleton key) {
                                            GuavaCache.LOGGER.trace("Missed cache for `getIoDevices`");
                                            return ioDevicesCloudApi.getIoDevices();
                                        }
                                    });
    }

    @Override
    public Device getIoDevice(final int id) {
        return getIoDevices()
                       .stream()
                       .filter(device -> device.getId() == id)
                       .findAny()
                       .orElseThrow(() -> new IllegalArgumentException("There is no IO Device with ID=" + id));
    }

    @Override
    public SortedSet<Device> getIoDevices() {
        try {
            return getIoDevicesCache.get(Singleton.KEY);
        } catch (ExecutionException e) {
            throw new RuntimeException("Cannot get all devices", e);
        }
    }
}
