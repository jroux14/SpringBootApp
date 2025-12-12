package com.smarthome.webapp.device.mqtt;

import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.device.domain.DeviceData;
import com.smarthome.webapp.device.service.DeviceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MqttInboundService {

    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;

    private Map<String, Object> asMap(JsonNode node) {
        if (node == null || node.isNull()) {
            return Map.of();
        }
        return objectMapper.convertValue(
            node,
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
        );
    }    

    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders()
            .get("mqtt_receivedTopic", String.class);

        if (topic == null) {
            return;
        }

        String payload = message.getPayload().toString();

        try {
            if ("smarthome/devices".equals(topic)) {
                handleDeviceAnnouncement(payload);
            } else {
                handlePeripheralUpdate(topic, payload);
            }
        } catch (Exception e) {
            // TODO: Implement logger
            // log.warn("Failed to process MQTT message. topic={}, payload={}", topic, payload, e);
            System.out.println(e);
        }
    }

    private void handleDeviceAnnouncement(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode data = root.get("data");
    
        if (data == null) return;
        if (!"online".equalsIgnoreCase(data.path("status").asText())) return;
    
        String deviceName = root.path("deviceName").asText(null);
        String deviceType = root.path("deviceType").asText(null);
        if (deviceName == null || deviceType == null) return;
    
        DeviceData deviceData = DeviceData.builder()
            .status(data.path("status").asText())
            .sensors(asMap(data.get("sensors")))
            .switches(asMap(data.get("switches")))
            .binarySensors(asMap(data.get("binarySensors")))
            .build();
    
        deviceService.registerOrUpdateFromAnnouncement(deviceName, deviceType, deviceData);
    }
    

    private void handlePeripheralUpdate(String topic, String payload) {
        String[] parts = topic.split("/");
        if (parts.length < 3) return;
    
        String deviceName = parts[0];
        String type = parts[1];
        String name = parts[2];
    
        Object value;
    
        if ("sensor".equals(type)) {
            try {
                value = Double.valueOf(payload.trim());
            } catch (NumberFormatException e) {
                return;
            }
        } else {
            // switches & binary sensors are strings
            value = payload.trim();
        }
    
        deviceService.processPeripheralUpdate(
            deviceName,
            type,
            name,
            value
        );
    }
}
