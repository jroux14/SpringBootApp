package com.smarthome.webapp.controllers;

import org.springframework.stereotype.Component;

import com.smarthome.webapp.interfaces.DeviceInterface;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.repositories.DeviceRepository;

@Component
public class DeviceController implements DeviceInterface {

    public DeviceController() {}

    @Override
    public void createDevice(String name, String userID, String mqttData, DeviceRepository repository) {
        Device thisDevice = new Device(name, userID, mqttData);
        repository.save(thisDevice);
    }
}
