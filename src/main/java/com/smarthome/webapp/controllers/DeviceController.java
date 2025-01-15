package com.smarthome.webapp.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.interfaces.DeviceInterface;
import com.smarthome.webapp.services.DeviceService;

@RestController
@RequestMapping("smarthome/device/")
public class DeviceController implements DeviceInterface {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<HashMap<String,Object>> registerDevice(@RequestBody HashMap<String,Object> device) {
        return this.deviceService.registerDevice(device);
    }

    @PostMapping("/updateItem")
    public ResponseEntity<HashMap<String,Object>> updateItem(@RequestBody HashMap<String,Object> request) {
        System.out.println(request);
        Object item = request.get("item");
        String deviceId = request.get("deviceId").toString();
        return this.deviceService.updateItem(deviceId, item);
    }

    // @Override
    // public void createDevice(Object device) {
    //     Device thisDevice = new Device(device);
    //     repository.save(thisDevice);
    // }
}
