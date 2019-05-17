package org.openhab.binding.supla.internal.cloud.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.Device;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class CaffeineIoDevicesCloudApi implements IoDevicesCloudApi {
    private final LoadingCache<GetIoDeviceKey, Device> getIoDeviceCache;
    private final LoadingCache<List<String>, List<Device>> getIoDevicesCache;

    CaffeineIoDevicesCloudApi(final IoDevicesCloudApi ioDevicesCloudApi) {
        getIoDeviceCache = Caffeine.newBuilder()
                                   .expireAfterWrite(CaffeineCache.cacheEvictTime, CaffeineCache.cacheEvictUnit)
                                   .build(key -> ioDevicesCloudApi.getIoDevice(key.id, key.include));
        getIoDevicesCache = Caffeine.newBuilder()
                                    .expireAfterWrite(CaffeineCache.cacheEvictTime, CaffeineCache.cacheEvictUnit)
                                    .build(ioDevicesCloudApi::getIoDevices);
    }

    @Override
    public Device getIoDevice(final int id, final List<String> include) throws ApiException {
        return getIoDeviceCache.get(new GetIoDeviceKey(id, include));
    }

    @Override
    public List<Device> getIoDevices(final List<String> include) throws ApiException {
        return getIoDevicesCache.get(include);
    }

    private static final class GetIoDeviceKey {
        final int id;
        final List<String> include;

        private GetIoDeviceKey(final int id, final List<String> include) {
            this.id = id;
            this.include = Collections.unmodifiableList(include);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof GetIoDeviceKey)) return false;
            final GetIoDeviceKey that = (GetIoDeviceKey) o;
            return id == that.id &&
                           Objects.equals(include, that.include);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "GetIoDeviceKey{" +
                           "id=" + id +
                           ", include=" + include +
                           '}';
        }
    }
}
