package com.smarthome.webapp.device.mqtt;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.smarthome.webapp.device.domain.DeviceCommandEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MqttCommandListener {

    private final DeviceCommandHandler commandHandler;

    @EventListener
    public void onDeviceCommand(DeviceCommandEvent event) {
        commandHandler.send(event.topic(), event.payload() == null ? "" : event.payload());
    }
}
