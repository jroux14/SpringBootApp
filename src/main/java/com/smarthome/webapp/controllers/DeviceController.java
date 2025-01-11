package com.smarthome.webapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.interfaces.DeviceInterface;

@RestController
public class DeviceController implements DeviceInterface {

    public DeviceController() {}

    // @Override
    // public void createDevice(Object device) {
    //     Device thisDevice = new Device(device);
    //     repository.save(thisDevice);
    // }
}
