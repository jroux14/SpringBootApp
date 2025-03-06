package com.smarthome.webapp.services;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.repositories.DeviceRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MqttPahoMessageDrivenChannelAdapter mqttInbound;

    @Autowired 
    private MessageChannel mqttOutboundChannel;

    @Bean
    public IntegrationFlow mqttInboundFlow() {
        return IntegrationFlow
            .from(mqttInbound)
            .handle(m -> {
                Object topicObj = m.getHeaders().get("mqtt_receivedTopic");
                Object payloadObj = m.getPayload();

                if (topicObj != null) {
                    String topic = topicObj.toString();

                    if (payloadObj != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
        
                        try {
                            JsonNode payloadJson = objectMapper.readValue(payloadObj.toString(), JsonNode.class);
                            JsonNode dataJson = payloadJson.get("data");

                            if (dataJson != null) {
                                String status = dataJson.get("status").asText();
                                if (status.equals("online")) {
                                    String deviceName = payloadJson.get("deviceName").asText();
                                    String deviceType = payloadJson.get("deviceType").asText();
                                    if (deviceName != null && deviceType != null) {
                                        if (this.getDeviceByName(deviceName) == null) {
                                            System.out.println("Received message on topic: " + topic + ", device name: " + deviceName);
                                            Device device = Device.builder()
                                                .deviceName(deviceName)
                                                .deviceType(deviceType)
                                                .data(objectMapper.treeToValue(dataJson, Object.class))
                                                .build();
    
                                            this.saveDevice(device);
                                        } else {
                                            System.out.println("Received message on topic: " + topic + ", device name: " + deviceName);
                                        }
                                    }

                                }
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            })
            .get();
    }

    public void addSubscription(String topic) {
        this.mqttInbound.addTopic(topic);
    }
    
    public void removeSubscription(String topic) {
       this.mqttInbound.removeTopic(topic);
    }

    public void sendMessage(String topic, String payload) {
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .build();
        mqttOutboundChannel.send(message);
    }

    public void saveDevice(Device device) {
        deviceRepository.save(device);
    }

    public Device getDeviceByName(String deviceName) {
        return this.deviceRepository.getDeviceByName(deviceName);
    }

    public Device[] getDevicesByUserId(String userId) {
        return this.deviceRepository.getDeviceByUserId(userId);
    }

    public Device getDeviceById(String deviceId) {
        return this.deviceRepository.getDeviceById(deviceId);
    }

    public ResponseEntity<String> testMqttAdd(String topic) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        this.addSubscription(topic);
        responseBody.put("success", true);

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> testMqttPub(String topic, String payload) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        this.sendMessage(topic, payload);
        responseBody.put("success", true);

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> getAvailableDevices() throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        Device[] unclaimedDevices = this.deviceRepository.getUnclaimedDevices();
        if (unclaimedDevices != null) {
            responseBody.put("success", true);
            responseBody.put("devices", unclaimedDevices);
        } else {
            responseBody.put("success", true);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> registerDevice(Device device) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if (device != null) {
                this.deviceRepository.claimDevice(device.getDeviceName(), device.getDeviceNameFriendly(), device.getItem(), device.getUserId());
                Device updatedDevice = this.deviceRepository.getDeviceByName(device.getDeviceName());
                responseBody.put("id", updatedDevice.getId());
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No Device");
                responseBody.put("success", false);
            }
        } catch(Exception e) {
            System.out.println(e);
            responseBody.put("error", "Unkown server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    /* TODO: Flesh this method out some more */
    public ResponseEntity<String> updateDevice(Device device) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        Optional<Device> existingDeviceOpt = deviceRepository.findById(device.getId());

        if (existingDeviceOpt.isPresent()) {
            Device existingDevice = existingDeviceOpt.get();
            existingDevice.setItem(device.getItem());
            this.deviceRepository.save(existingDevice);

            responseBody.put("success", true);
        } else {
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> deleteDevice(Device device) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        this.deviceRepository.deleteById(device.getId());

        responseBody.put("success", true);
        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
