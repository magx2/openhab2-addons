package org.openhab.binding.supla.internal.cloud.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import pl.grzeslowski.jsupla.api.channel.Channel;
import pl.grzeslowski.jsupla.api.channel.action.Action;

import java.util.concurrent.ExecutionException;

final class GuavaChannelsCloudApi implements ChannelsCloudApi {
    private final ChannelsCloudApi channelsCloudApi;
    private final LoadingCache<Integer, Channel> getChannelCache;

    GuavaChannelsCloudApi(final ChannelsCloudApi channelsCloudApi) {
        this.channelsCloudApi = channelsCloudApi;
        getChannelCache = CacheBuilder.newBuilder()
                                  .expireAfterWrite(GuavaCache.cacheEvictTime, GuavaCache.cacheEvictUnit)
                                  .build(new CacheLoader<Integer, Channel>() {
                                      @Override
                                      public Channel load(final Integer id) {
                                          GuavaCache.LOGGER.trace("Missed cache for `getChannel`");
                                          return channelsCloudApi.getChannel(id);
                                      }
                                  });
    }

    @Override
    public void executeAction(final Channel channel, final Action action) {
        channelsCloudApi.executeAction(channel, action);
    }

    @Override
    public Channel getChannel(final int id) {
        try {
            return getChannelCache.get(id);
        } catch (ExecutionException e) {
            throw new RuntimeException("Cannot get channel for key=" + id, e);
        }
    }
}
