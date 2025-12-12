package com.smarthome.webapp.device.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarthome.webapp.constants.DeviceConstants;
import com.smarthome.webapp.device.domain.Device;
import com.smarthome.webapp.device.domain.DeviceCommandEvent;
import com.smarthome.webapp.device.domain.DeviceData;
import com.smarthome.webapp.device.domain.DeviceRepository;
import com.smarthome.webapp.device.domain.PeripheralType;
import com.smarthome.webapp.device.domain.exception.DeviceNotFoundException;
import com.smarthome.webapp.device.domain.exception.UnauthorizedDeviceAccessException;
import com.smarthome.webapp.sensor_reading.repository.SensorReadingDocument;
import com.smarthome.webapp.sensor_reading.repository.SensorReadingMetadata;
import com.smarthome.webapp.sensor_reading.repository.SensorReadingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final ApplicationEventPublisher events;

    public void processMqttCommand(String topic, String payload) {
        events.publishEvent(new DeviceCommandEvent(topic, payload));
    }

    public Device registerOrUpdateFromAnnouncement(String deviceName, String deviceType, DeviceData data) {
        return deviceRepository.findByName(deviceName)
            .orElseGet(() -> {
                Device device = Device.builder()
                    .deviceName(deviceName)
                    .deviceType(deviceType)
                    .data(data)
                    .build();
                return deviceRepository.save(device);
            });
    }

    public Optional<PeripheralType> resolvePeripheralType(String rawType, String name) {
        return switch (rawType) {
            case "sensor" -> DeviceConstants.VALID_OUTLET_SENSORS.contains(name)
                ? Optional.of(PeripheralType.SENSOR)
                : Optional.empty();
    
            case "switch" -> Optional.of(PeripheralType.SWITCH);
    
            case "binary_sensor" -> Optional.of(PeripheralType.BINARY_SENSOR);
    
            default -> Optional.empty();
        };
    }

    public void processPeripheralUpdate(String deviceName, String peripheralType, String peripheralName, Object rawValue) {
        Device device = deviceRepository.findByName(deviceName)
            .orElseThrow(() -> new DeviceNotFoundException(deviceName));

        PeripheralType.resolve(peripheralType, peripheralName)
            .ifPresent(type -> {
                DeviceData updated =
                    device.getData().withUpdate(
                        type,
                        peripheralName,
                        rawValue
                    );
        
                device.updateData(updated);
                deviceRepository.save(device);
        
                if (type == PeripheralType.SENSOR) {
                    recordSensorReading(
                        device.getId(),
                        "sensor",
                        peripheralName,
                        rawValue
                    );
                }
            });
    }


    public ResponseEntity<String> getSensorReadingsByDeviceId(String deviceId, Instant start, Instant end) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        try {
            List<SensorReadingDocument> sensorReadings = sensorReadingRepository.findReadingsByDeviceId(deviceId, start, end);

            if (sensorReadings != null) {
                responseBody.put("data", sensorReadings);
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No sensor data");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Failed to fetch sensor data");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public Device registerNewDevice(String deviceName, String deviceType, DeviceData data) {
        Device device = Device.builder()
            .deviceName(deviceName)
            .deviceType(deviceType)
            .data(data)
            .build();

        return deviceRepository.save(device);
    }

    public void registerDevice(String userId, String deviceName, String friendlyName, String roomId) {
        Device device = deviceRepository.findByName(deviceName)
            .orElseThrow(() -> new DeviceNotFoundException(deviceName));

        device.claim(userId);
        device.assignRoom(roomId);
        device.setDeviceNameFriendly(friendlyName);

        deviceRepository.save(device);
    }

    public Device claimDevice(String deviceName, String userId, String friendlyName, String roomId) {
        Device device = deviceRepository.findByName(deviceName)
            .orElseThrow(() -> new IllegalStateException("Device not found"));

        device.claim(userId);
        device.assignRoom(roomId);

        return deviceRepository.save(device);
    }

    public void updateDeviceData(String deviceId, DeviceData data) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalStateException("Device not found"));

        device.updateData(data);
        deviceRepository.save(device);
    }

    public List<Device> getDevicesByUser(String userId) {
        return deviceRepository.findByUserId(userId);
    }

    public Optional<Device> getDeviceById(String deviceId) {
        return deviceRepository.findById(deviceId);
    }

    public Optional<Device> getDeviceByName(String deviceName) {
        return deviceRepository.findByName(deviceName);
    }

    public List<Device> getUnclaimedDevices() {
        return deviceRepository.findUnclaimed();
    }

    public void deleteDevice(String deviceId, String userId) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    
        if (!userId.equals(device.getUserId())) {
            throw new UnauthorizedDeviceAccessException(deviceId);
        }
    
        deviceRepository.deleteById(deviceId);
    }

    /* Sensor history */
    public void recordSensorReading(String deviceId, String type, String name, Object value) {
        SensorReadingMetadata metadata =
            new SensorReadingMetadata(deviceId, name, type);

        sensorReadingRepository.save(
            new SensorReadingDocument(Instant.now(), metadata, value)
        );
    }
}
