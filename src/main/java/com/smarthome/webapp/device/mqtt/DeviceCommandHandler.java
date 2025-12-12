package com.smarthome.webapp.device.mqtt;

public interface DeviceCommandHandler {
    void send(String topic, String payload);
}
