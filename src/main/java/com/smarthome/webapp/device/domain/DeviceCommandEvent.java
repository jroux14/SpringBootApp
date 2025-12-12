package com.smarthome.webapp.device.domain;

public record DeviceCommandEvent(String topic, String payload) {}
