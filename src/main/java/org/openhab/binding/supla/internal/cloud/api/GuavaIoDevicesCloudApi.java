package org.openhab.binding.supla.internal.cloud.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.device.Device;

import java.util.SortedSet;
import java.util.concurrent.ExecutionException;

final class GuavaIoDevicesCloudApi implements IoDevicesCloudApi {
    private final LoadingCache<Integer, Device> getIoDeviceCache;
    private final LoadingCache<Singleton, SortedSet<Device>> getIoDevicesCache;

    private enum Singleton {
        KEY
    }

    GuavaIoDevicesCloudApi(final IoDevicesCloudApi ioDevicesCloudApi) {
        getIoDeviceCache = CacheBuilder.newBuilder()
                                   .expireAfterWrite(GuavaCache.cacheEvictTime, GuavaCache.cacheEvictUnit)
                                   .build(new CacheLoader<Integer, Device>() {
                                       @Override
                                       public Device load(final Integer id) {
                                           GuavaCache.LOGGER.trace("Missed cache for `getIoDevice`");
                                           return ioDevicesCloudApi.getIoDevice(id);
                                       }
                                   });
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
        try {
            return getIoDeviceCache.get(id);
        } catch (ExecutionException e) {
            throw new RuntimeException("Cannot get device with id=" + id, e);
        }
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
