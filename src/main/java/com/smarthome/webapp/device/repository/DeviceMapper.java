package com.smarthome.webapp.device.repository;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smarthome.webapp.device.domain.Device;
import com.smarthome.webapp.device.domain.DeviceData;

final class DeviceMapper {

    static Device toDomain(DeviceDocument doc) {
        DeviceData data = null;

        Map<String, Object> dataMap = doc.getData();
        if (dataMap != null) {
            data = DeviceData.builder()
                .status((String) dataMap.get("status"))
                .sensors(castMap(dataMap.get("sensors")))
                .switches(castMap(dataMap.get("switches")))
                .binarySensors(castMap(dataMap.get("binarySensors")))
                .build();
        }

        return Device.builder()
            .id(doc.getId())
            .userId(doc.getUserId())
            .deviceName(doc.getDeviceName())
            .deviceNameFriendly(doc.getDeviceNameFriendly())
            .roomId(doc.getRoomId())
            .deviceType(doc.getDeviceType())
            .data(data)
            .build();
    }

    static DeviceDocument toDocument(Device device, ObjectMapper mapper) {
        Map<String, Object> dataMap = null;
    
        DeviceData data = device.getData();
        if (data != null) {
            ObjectNode node = mapper.createObjectNode();
    
            node.put("status", data.getStatus());
            if (data.getSensors() != null) {
                node.set("sensors", mapper.valueToTree(data.getSensors()));
            }
            if (data.getSwitches() != null) {
                node.set("switches", mapper.valueToTree(data.getSwitches()));
            }
            if (data.getBinarySensors() != null) {
                node.set("binarySensors", mapper.valueToTree(data.getBinarySensors()));
            }
            dataMap = mapper.convertValue(
                node,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );
        }
    
        return DeviceDocument.builder()
            .id(device.getId())
            .userId(device.getUserId())
            .deviceName(device.getDeviceName())
            .deviceNameFriendly(device.getDeviceNameFriendly())
            .roomId(device.getRoomId())
            .deviceType(device.getDeviceType())
            .data(dataMap)
            .build();
    }
    

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return value instanceof Map
            ? (Map<String, Object>) value
            : null;
    }
}
