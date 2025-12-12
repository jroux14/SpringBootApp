package com.smarthome.webapp.device.mqtt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MqttOutboundService implements DeviceCommandHandler {

    @Qualifier("mqttOutboundChannel")
    private final MessageChannel mqttOutboundChannel;

    @Override
    public void send(String topic, String payload) {
        mqttOutboundChannel.send(
            MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .build()
        );
    }
}

