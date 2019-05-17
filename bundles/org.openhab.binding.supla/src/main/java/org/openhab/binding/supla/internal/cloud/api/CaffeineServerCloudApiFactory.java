package org.openhab.binding.supla.internal.cloud.api;

final class CaffeineServerCloudApiFactory implements ServerCloudApiFactory {
    private final ServerCloudApiFactory serverCloudApiFactory;

    CaffeineServerCloudApiFactory(final ServerCloudApiFactory serverCloudApiFactory) {
        this.serverCloudApiFactory = serverCloudApiFactory;
    }

    @Override
    public ServerCloudApi newServerCloudApi(final String token) {
        return new CaffeineServerCloudApi(serverCloudApiFactory.newServerCloudApi(token));
    }
}
