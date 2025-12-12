package com.smarthome.webapp.device.mqtt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
public class MqttSubscriptionHandler implements MqttSubscriptionPort {

    private final MqttPahoMessageDrivenChannelAdapter inbound;

    public MqttSubscriptionHandler(
        @Qualifier("mqttInbound") MqttPahoMessageDrivenChannelAdapter inbound
    ) {
        this.inbound = inbound;
    }

    @Override
    public void subscribe(String topic) {
        inbound.addTopic(topic);
    }

    @Override
    public void unsubscribe(String topic) {
        inbound.removeTopic(topic);
    }
}