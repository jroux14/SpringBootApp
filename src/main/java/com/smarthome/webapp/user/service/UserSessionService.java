package com.smarthome.webapp.user.service;

import org.springframework.stereotype.Service;

import com.smarthome.webapp.device.domain.DeviceRepository;
import com.smarthome.webapp.device.mqtt.MqttSubscriptionPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final DeviceRepository deviceRepository;
    private final MqttSubscriptionPort mqttSubscriptions;

    public void onLogin(String userId) {
        deviceRepository.findByUserId(userId)
            .forEach(device ->
                mqttSubscriptions.subscribe(device.getDeviceName() + "/#")
            );
    }

    public void onLogout(String userId) {
        deviceRepository.findByUserId(userId)
            .forEach(device ->
                mqttSubscriptions.unsubscribe(device.getDeviceName() + "/#")
            );
    }
}
