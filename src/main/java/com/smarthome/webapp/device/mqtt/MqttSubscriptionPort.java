package com.smarthome.webapp.device.mqtt;

public interface MqttSubscriptionPort {
    void subscribe(String topic);
    void unsubscribe(String topic);
}
