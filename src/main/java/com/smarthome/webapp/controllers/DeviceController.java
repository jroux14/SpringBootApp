package com.smarthome.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smarthome.webapp.interfaces.DeviceInterface;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.repositories.DeviceRepository;

@Component
public class DeviceController implements DeviceInterface {

    public DeviceController() {}

    @Override
    public void createDevice(String name, String userID, String mqttData, DeviceRepository repository) {
        repository.save(new Device(name, userID, mqttData));
    }
}
