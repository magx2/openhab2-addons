package org.openhab.binding.supla.internal.cloud.api;


import pl.grzeslowski.jsupla.api.Api;
import pl.grzeslowski.jsupla.api.DeviceApi;
import pl.grzeslowski.jsupla.api.device.Device;

import java.util.SortedSet;

final class SwaggerIoDevicesCloudApi implements IoDevicesCloudApi {
    private final DeviceApi ioDevicesApi;

    SwaggerIoDevicesCloudApi(final Api api) {
        ioDevicesApi = api.getDeviceApi();
    }

    @Override
    public Device getIoDevice(final int id) {
        return ioDevicesApi.findDevice(id);
    }

    @Override
    public SortedSet<Device> getIoDevices() {
        return ioDevicesApi.findDevices();
    }
}
