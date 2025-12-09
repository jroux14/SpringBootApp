package com.smarthome.webapp.controllers;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.interfaces.DeviceInterface;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.services.AggregationService;
import com.smarthome.webapp.services.AuthService;
import com.smarthome.webapp.services.DeviceService;

@RestController
@RequestMapping("smarthome/device/")
public class DeviceController implements DeviceInterface {

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AuthService authService;

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

    @GetMapping("get/data")
    public ResponseEntity<String> getDeviceData(@RequestHeader("Authorization") String token) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

        String jwt = token.substring(7);
        String username = this.authService.getJwtSubject(jwt);

        try {
            String userId = this.authService.getUserIdFromUsername(username);
            resp = this.deviceService.getUserDeviceData(userId);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("get/data/{deviceId}")
    public ResponseEntity<String> getDeviceDataByDeviceName(@PathVariable("deviceId") String deviceId) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

        try {
            resp = this.deviceService.getDeviceDataById(deviceId);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("/get/sensor/data/{deviceId}")
    public ResponseEntity<String> getSensorReadingsByDeviceId(@PathVariable("deviceId") String deviceId, @RequestParam String start, @RequestParam String end) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

        try {
            Instant startTime = Instant.parse(start);
            Instant endTime = Instant.parse(end);
            resp = this.deviceService.getSensorReadingsByDeviceId(deviceId, startTime, endTime);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("/get/{sensorName}/data/{deviceId}/hourly")
    public ResponseEntity<String> getHourlySensorReadingsByDeviceId(@PathVariable("deviceId") String deviceId, @PathVariable("sensorName") String sensorName, @RequestParam("start") String startIso, @RequestParam("end") String endIso) {
        ResponseEntity<String> resp = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    
        try {
            Instant start = Instant.parse(startIso);
            Instant end = Instant.parse(endIso);
            resp = this.aggregationService.getHourlyForDay(deviceId, sensorName, start, end);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    
        return resp;
    }
    @GetMapping("/get/{sensorName}/data/{deviceId}/daily")
    public ResponseEntity<String> getDailySensorReadingsByDeviceId(@PathVariable("deviceId") String deviceId, @PathVariable("sensorName") String sensorName, @RequestParam("start") String startIso, @RequestParam("end") String endIso) {
        ResponseEntity<String> resp = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    
        try {
            Instant start = Instant.parse(startIso);
            Instant end = Instant.parse(endIso);
            resp = this.aggregationService.getDailyForMonth(deviceId, sensorName, start, end);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    
        return resp;
    }
    

    @GetMapping("/get/{sensorName}/data/{deviceId}/monthly")
    public ResponseEntity<String> getMonthlySensorReadingsByDeviceId(@PathVariable("deviceId") String deviceId, @PathVariable("sensorName") String sensorName, @RequestParam("start") String startIso, @RequestParam("end") String endIso) {
        ResponseEntity<String> resp = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    
        try {
            Instant start = Instant.parse(startIso);
            Instant end = Instant.parse(endIso);
            resp = this.aggregationService.getMonthlyForYear(deviceId, sensorName, start, end);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    
        return resp;
    }
    

    @GetMapping("/get/{sensorName}/data/{deviceId}/yearly")
    public ResponseEntity<String> getYearlySensorReadingsByDeviceId(@PathVariable("deviceId") String deviceId, @PathVariable("sensorName") String sensorName, @RequestParam int start, @RequestParam int end) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

        try {
            resp = this.aggregationService.getYearlyForRange(deviceId, sensorName, start, end);
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
}
