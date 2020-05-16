package org.openhab.binding.supla.internal.cloud.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.serverinfo.ServerInfo;

import java.util.concurrent.ExecutionException;

final class GuavaServerCloudApi implements ServerCloudApi {
    private final LoadingCache<String, ServerInfo> getServerInfoCache;

    public GuavaServerCloudApi(final ServerCloudApi serverCloudApi) {
        getServerInfoCache = CacheBuilder.newBuilder()
                                     .expireAfterWrite(GuavaCache.cacheEvictTime, GuavaCache.cacheEvictUnit)
                                     .build(new CacheLoader<String, ServerInfo>() {
                                         @Override
                                         public ServerInfo load(final String __) {
                                             GuavaCache.LOGGER.trace("Missed cache for `getServerInfo`");
                                             return serverCloudApi.getServerInfo();
                                         }
                                     });
    }

    @Override
    public ServerInfo getServerInfo() {
        try {
            return getServerInfoCache.get("getServerInfo()");
        } catch (ExecutionException e) {
            throw new RuntimeException("Cannot get server info", e);
        }
    }
}
