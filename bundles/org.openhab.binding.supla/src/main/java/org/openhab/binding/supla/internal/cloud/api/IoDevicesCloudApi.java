package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.model.Device;

import java.util.List;

public interface IoDevicesCloudApi {
    Device getIoDevice(Integer id, List<String> include) throws ApiException;

    List<Device> getIoDevices(List<String> include) throws ApiException;
}
