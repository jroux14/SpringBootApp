package com.smarthome.webapp.device.controller.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceDTO {
    private final String id;
    private final String deviceType;
    private final String userId;
    private final String deviceName;
    private final String deviceNameFriendly;
    private final String roomId;
    private final JsonNode data;
}

