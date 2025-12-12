package com.smarthome.webapp.device.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.aggregation.service.AggregationService;
import com.smarthome.webapp.auth.service.AuthService;
import com.smarthome.webapp.device.service.DeviceService;

import lombok.RequiredArgsConstructor;

import com.smarthome.webapp.device.controller.dto.DeviceDTOMapper;
import com.smarthome.webapp.device.controller.dto.MqttMessageRequest;
import com.smarthome.webapp.device.controller.dto.RegisterDeviceRequest;
import com.smarthome.webapp.device.domain.Device;
import com.smarthome.webapp.device.controller.dto.DeviceDTO;

@RequiredArgsConstructor
@RestController
@RequestMapping("smarthome/device/")
public class DeviceController {

    private final AggregationService aggregationService;
    private final DeviceService deviceService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    // @PostMapping("add/topic")
    // public ResponseEntity<String> addTopic(@RequestBody String messageData) {
    //     ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    //     ObjectMapper objectMapper = new ObjectMapper();

    //     try {
    //         JsonNode messageJson = objectMapper.readValue(messageData, JsonNode.class);
    //         String topic = messageJson.get("topic").asText();

    //         resp = this.deviceService.testMqttAdd(topic);
    //     } catch (JsonMappingException e) {
    //         e.printStackTrace();
    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //     }

    //     return resp;
    // }

    @PostMapping("send/message")
    public void sendMessage(@RequestBody MqttMessageRequest messageData) {
        this.deviceService.processMqttCommand(messageData.topic(), messageData.message());
    }

    @GetMapping("get/available")
    public List<Device> getAvailableDevices() {
        return this.deviceService.getUnclaimedDevices();
    }

    @GetMapping("get/data")
    public List<DeviceDTO> getUserDevices(@RequestHeader("Authorization") String token) {
        String userId = authService.getUserIdFromUsername(authService.getJwtSubject(token.substring(7)));

        return deviceService.getDevicesByUser(userId).stream()
            .map(device -> DeviceDTOMapper.toDto(device, objectMapper))
            .toList();
    }

    @GetMapping("get/data/{deviceId}")
    public DeviceDTO getDevice(@PathVariable String deviceId) {
        Device device = deviceService.getDeviceById(deviceId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Device not found"
                )
            );
    
        return DeviceDTOMapper.toDto(device, objectMapper);
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

    @PostMapping("/register")
    public void registerDevice(@RequestBody RegisterDeviceRequest request, @RequestHeader("Authorization") String token) {
        String userId = authService.getUserIdFromUsername(authService.getJwtSubject(token.substring(7)));
    
        deviceService.registerDevice(userId, request.deviceName(), request.deviceNameFriendly(), request.roomId());
    }

    @DeleteMapping("delete/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(
        @PathVariable String deviceId,
        @RequestHeader("Authorization") String token
    ) {
        String userId = authService.getUserIdFromUsername(
            authService.getJwtSubject(token.substring(7))
        );
    
        deviceService.deleteDevice(deviceId, userId);
    }
    
}
