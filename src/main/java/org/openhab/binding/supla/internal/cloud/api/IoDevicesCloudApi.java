package org.openhab.binding.supla.internal.cloud.api;


import pl.grzeslowski.jsupla.api.device.Device;

import java.util.SortedSet;

public interface IoDevicesCloudApi {
    Device getIoDevice(int id);

    SortedSet<Device> getIoDevices();
}
