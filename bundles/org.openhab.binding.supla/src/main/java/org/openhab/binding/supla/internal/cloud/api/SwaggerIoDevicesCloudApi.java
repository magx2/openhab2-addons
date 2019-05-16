package org.openhab.binding.supla.internal.cloud.api;

import pl.grzeslowski.jsupla.api.generated.ApiClient;
import pl.grzeslowski.jsupla.api.generated.ApiException;
import pl.grzeslowski.jsupla.api.generated.api.IoDevicesApi;
import pl.grzeslowski.jsupla.api.generated.model.Device;

import java.util.List;

class SwaggerIoDevicesCloudApi implements IoDevicesCloudApi {
    private final IoDevicesApi ioDevicesApi;

    SwaggerIoDevicesCloudApi(final ApiClient apiClient) {
        ioDevicesApi = new IoDevicesApi(apiClient);
    }

    @Override
    public Device getIoDevice(final Integer id, final List<String> include) throws ApiException {
        return ioDevicesApi.getIoDevice(id, include);
    }
}
