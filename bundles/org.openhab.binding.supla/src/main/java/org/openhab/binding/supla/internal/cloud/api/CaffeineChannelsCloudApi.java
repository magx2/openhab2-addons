package org.openhab.binding.supla.internal.cloud.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.Channel;
import pl.grzeslowski.jsupla.api.generated.model.ChannelExecuteActionRequest;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

final class CaffeineChannelsCloudApi implements ChannelsCloudApi {
    private final ChannelsCloudApi channelsCloudApi;
    private final LoadingCache<GetChannelKey, Channel> getChannelCache;

    CaffeineChannelsCloudApi(final ChannelsCloudApi channelsCloudApi) {
        this.channelsCloudApi = channelsCloudApi;
        getChannelCache = Caffeine.newBuilder()
                                  .expireAfterWrite(CaffeineCache.cacheEvictTime, CaffeineCache.cacheEvictUnit)
                                  .build(key -> channelsCloudApi.getChannel(key.id, key.include));
    }

    @Override
    public void executeAction(final ChannelExecuteActionRequest body, final Integer id) throws ApiException {
        channelsCloudApi.executeAction(body, id);
    }

    @Override
    public Channel getChannel(final int id, final List<String> include) throws ApiException {
        return getChannelCache.get(new GetChannelKey(id, include));
    }

    private static final class GetChannelKey {
        final int id;
        final List<String> include;

        private GetChannelKey(final int id, final List<String> include) {
            this.id = id;
            this.include = unmodifiableList(include);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof GetChannelKey)) return false;
            final GetChannelKey that = (GetChannelKey) o;
            return id == that.id &&
                           Objects.equals(include, that.include);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "GetChannelKey{" +
                           "id=" + id +
                           ", include=" + include +
                           '}';
        }
    }
}
