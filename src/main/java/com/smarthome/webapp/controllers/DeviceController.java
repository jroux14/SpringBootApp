package com.smarthome.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.interfaces.DeviceInterface;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.services.DeviceService;

@RestController
@RequestMapping("smarthome/device/")
public class DeviceController implements DeviceInterface {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("register")
    public ResponseEntity<String> registerDevice(@RequestBody String deviceStr) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode deviceJson = objectMapper.readValue(deviceStr, JsonNode.class);
            Device device = objectMapper.treeToValue(deviceJson, Device.class);

            if (device != null) {
                resp = this.deviceService.registerDevice(device);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return resp;
    }

    @PostMapping("delete")
    public ResponseEntity<String> deleteDevice(@RequestBody String deviceStr) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            JsonNode deviceJson = objectMapper.readValue(deviceStr, JsonNode.class);
            Device device = objectMapper.treeToValue(deviceJson, Device.class);

            if (device != null) {
                resp = this.deviceService.deleteDevice(device);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return resp;
    }


    @PostMapping("update/position")
    public ResponseEntity<String> updateDevicePosition(@RequestBody String deviceStr) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode deviceJson = objectMapper.readValue(deviceStr, JsonNode.class);
            Device device = objectMapper.treeToValue(deviceJson, Device.class);

            if (device != null) {
                resp = this.deviceService.updateDevice(device);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return resp;
    }
}
