package com.smarthome.webapp.device.controller.dto;

public record MqttMessageRequest (
    String topic,
    String message
) {}
