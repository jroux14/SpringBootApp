package com.smarthome.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("add/topic")
    public ResponseEntity<String> addTopic(@RequestBody String messageData) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode messageJson = objectMapper.readValue(messageData, JsonNode.class);
            String topic = messageJson.get("topic").asText();

            resp = this.deviceService.testMqttAdd(topic);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @PostMapping("send/message")
    public ResponseEntity<String> sendMessage(@RequestBody String messageData) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode messageJson = objectMapper.readValue(messageData, JsonNode.class);
            String topic = messageJson.get("topic").asText();
            String payload = messageJson.get("payload").asText();

            resp = this.deviceService.testMqttPub(topic, payload);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("get/available")
    public ResponseEntity<String> getAvailableDevices() {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

        try {
            resp = this.deviceService.getAvailableDevices();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("get/data/{deviceName}")
    public ResponseEntity<String> getDeviceData(@PathVariable String deviceName) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            resp = this.deviceService.getDeviceDataByName(deviceName);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

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
