package com.smarthome.webapp.controllers;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.interfaces.DeviceInterface;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.repositories.DeviceRepository;

@RestController
public class DeviceController implements DeviceInterface {

    public DeviceController() {}

    // @Override
    // public void createDevice(Object device) {
    //     Device thisDevice = new Device(device);
    //     repository.save(thisDevice);
    // }
}
