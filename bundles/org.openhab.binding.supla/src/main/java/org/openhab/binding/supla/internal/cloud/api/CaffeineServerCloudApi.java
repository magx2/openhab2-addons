package org.openhab.binding.supla.internal.cloud.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.ServerInfo;

final class CaffeineServerCloudApi implements ServerCloudApi {
    private final LoadingCache<String, ServerInfo> getServerInfoCache;

    public CaffeineServerCloudApi(final ServerCloudApi serverCloudApi) {
        getServerInfoCache = Caffeine.newBuilder()
                                     .expireAfterWrite(CaffeineCache.cacheEvictTime, CaffeineCache.cacheEvictUnit)
                                     .build(key -> serverCloudApi.getServerInfo());
    }

    @Override
    public ServerInfo getServerInfo() throws ApiException {
        return getServerInfoCache.get("getServerInfo()");
    }
}
