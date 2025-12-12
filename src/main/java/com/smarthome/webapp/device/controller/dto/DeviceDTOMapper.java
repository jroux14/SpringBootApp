package com.smarthome.webapp.device.controller.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smarthome.webapp.device.domain.Device;
import com.smarthome.webapp.device.domain.DeviceData;

public final class DeviceDTOMapper {

    private DeviceDTOMapper() {}

    public static DeviceDTO toDto(Device device, ObjectMapper mapper) {
        if (device == null) {
            return null;
        }

        return DeviceDTO.builder()
            .id(device.getId())
            .deviceType(device.getDeviceType())
            .userId(device.getUserId())
            .deviceName(device.getDeviceName())
            .deviceNameFriendly(device.getDeviceNameFriendly())
            .roomId(device.getRoomId())
            .data(toJson(device.getData(), mapper))
            .build();
    }

    private static JsonNode toJson(DeviceData data, ObjectMapper mapper) {
        if (data == null) {
            return null;
        }
    
        ObjectNode root = mapper.createObjectNode();
        root.put("status", data.getStatus());
    
        if (data.getSensors() != null) {
            root.set("sensors", mapper.valueToTree(data.getSensors()));
        }
        if (data.getSwitches() != null) {
            root.set("switches", mapper.valueToTree(data.getSwitches()));
        }
        if (data.getBinarySensors() != null) {
            root.set("binarySensors", mapper.valueToTree(data.getBinarySensors()));
        }
    
        return root;
    }    
}
