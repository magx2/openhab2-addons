package org.openhab.binding.supla.internal.cloud.api;

final class CaffeineIoDevicesCloudApiFactory implements IoDevicesCloudApiFactory {
    private final IoDevicesCloudApiFactory ioDevicesCloudApiFactory;

    CaffeineIoDevicesCloudApiFactory(final IoDevicesCloudApiFactory ioDevicesCloudApiFactory) {
        this.ioDevicesCloudApiFactory = ioDevicesCloudApiFactory;
    }

    @Override
    public IoDevicesCloudApi newIoDevicesCloudApi(final String token) {
        return new CaffeineIoDevicesCloudApi(ioDevicesCloudApiFactory.newIoDevicesCloudApi(token));
    }
}
