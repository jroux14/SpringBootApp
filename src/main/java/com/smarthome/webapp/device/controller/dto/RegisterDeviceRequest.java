package com.smarthome.webapp.device.controller.dto;

public record RegisterDeviceRequest(
    String deviceName,
    String deviceNameFriendly,
    String roomId
) {}
