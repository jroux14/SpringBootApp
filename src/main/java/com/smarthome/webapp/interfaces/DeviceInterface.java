package com.smarthome.webapp.interfaces;

import com.smarthome.webapp.repositories.DeviceRepository;

public interface DeviceInterface {
    public void createDevice(String name, String userID, String mqttData, DeviceRepository repository);
}
