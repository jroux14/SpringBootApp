package com.smarthome.webapp.device.domain;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository {

    Optional<Device> findById(String id);

    Optional<Device> findByName(String deviceName);

    List<Device> findByUserId(String userId);

    List<Device> findUnclaimed();

    Device save(Device device);

    void deleteById(String id);
}
