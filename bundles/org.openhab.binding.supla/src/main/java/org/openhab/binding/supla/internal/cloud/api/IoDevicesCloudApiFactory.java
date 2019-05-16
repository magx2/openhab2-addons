package org.openhab.binding.supla.internal.cloud.api;

@FunctionalInterface
public interface IoDevicesCloudApiFactory {
    IoDevicesCloudApi newIoDevicesCloudApi(String token);
}
