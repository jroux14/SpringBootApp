package com.smarthome.webapp.services;

import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.util.NumberUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
                            if (topic.equals("smarthome/devices")) {
                                JsonNode payloadJson = objectMapper.readValue(payloadObj.toString(), JsonNode.class);
                                this.newDeviceConnected(payloadJson);
                            } else {
                                /* 
                                 * Typical format of topics other than smarthome/devices should be
                                 * {deviceName}/{peripheralType}/{peripheralName}/status
                                 */
                                String[] topicArr = topic.split("/");
                                String deviceName = topicArr[0];

                                Device device = this.getDeviceByName(deviceName);
                                if (device != null) {
                                    JsonNode dataNode = objectMapper.valueToTree(device.getData());

                                    if (!dataNode.isObject()) {
                                        dataNode = objectMapper.createObjectNode();
                                    }
                                    ObjectNode dataObj = (ObjectNode) dataNode;

                                    if (topicArr[1].equals("sensor")) {
                                        JsonNode sensorNode = dataNode.get("sensors");
                                        if (sensorNode == null || !sensorNode.isObject()) {
                                            sensorNode = objectMapper.createObjectNode();
                                        }
                                        ObjectNode sensorObj = (ObjectNode) sensorNode;

                                        String sensorName = topicArr[2];
                                        String sensorData = payloadObj.toString();
                                        if (StringUtils.isNumeric(sensorData)) {
                                            int sensorNumData = Integer.parseInt(sensorData);
                                            sensorObj.put(sensorName, sensorNumData);
                                        } else {
                                            sensorObj.put(sensorName, sensorData);
                                        }
                                        dataObj.set("sensors", sensorObj);

                                        this.deviceRepository.updateDeviceData(device.getDeviceName(), objectMapper.treeToValue(dataObj, Object.class));
                                    } else if (topicArr[1].equals("binary_sensor")) {
                                        JsonNode binarySensorNode = dataNode.get("binarySensors");
                                        if (binarySensorNode == null || !binarySensorNode.isObject()) {
                                            binarySensorNode = objectMapper.createObjectNode();
                                        }
                                        ObjectNode binarySensorObj = (ObjectNode) binarySensorNode;

                                        String binarySensorName = topicArr[2];
                                        String binarySensorData = payloadObj.toString();
                                        if (StringUtils.isNumeric(binarySensorData)) {
                                            int binarySensorNumData = Integer.parseInt(binarySensorData);
                                            binarySensorObj.put(binarySensorName, binarySensorNumData);
                                        } else {
                                            binarySensorObj.put(binarySensorName, binarySensorData);
                                        }
                                        dataObj.set("binarySensors", binarySensorObj);

                                        this.deviceRepository.updateDeviceData(device.getDeviceName(), objectMapper.treeToValue(dataObj, Object.class));
                                    } else if (topicArr[1].equals("switch")) {
                                        JsonNode switchNode = dataNode.get("switches");
                                        if (switchNode == null || !switchNode.isObject()) {
                                            switchNode = objectMapper.createObjectNode();
                                        }
                                        ObjectNode switchObj = (ObjectNode) switchNode;

                                        String switchName = topicArr[2];
                                        String switchData = payloadObj.toString();
                                        if (StringUtils.isNumeric(switchData)) {
                                            int switchNumData = Integer.parseInt(switchData);
                                            switchObj.put(switchName, switchNumData);
                                        } else {
                                            switchObj.put(switchName, switchData);
                                        }
                                        dataObj.set("switches", switchObj);

                                        this.deviceRepository.updateDeviceData(device.getDeviceName(), objectMapper.treeToValue(dataObj, Object.class));
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

    public void newDeviceConnected(JsonNode payloadJson) throws JsonProcessingException, IllegalArgumentException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataJson = payloadJson.get("data");

        if (dataJson != null) {
            String status = dataJson.get("status").asText();
            if (status.equals("online")) {
                String deviceName = payloadJson.get("deviceName").asText();
                String deviceType = payloadJson.get("deviceType").asText();
                if (deviceName != null && deviceType != null) {
                    if (this.getDeviceByName(deviceName) == null) {
                        Device device = Device.builder()
                            .deviceName(deviceName)
                            .deviceType(deviceType)
                            .data(objectMapper.treeToValue(dataJson, Object.class))
                            .build();

                        this.saveDevice(device);
                    }
                }
            }
        }
    }

    public void addSubscription(String newTopic) {
        Boolean topicExists = false;
        for (String topic : mqttInbound.getTopic()) {
            if (topic.equals(newTopic)) {
                topicExists = true;
            }
        }

        if (!topicExists) {
            this.mqttInbound.addTopic(newTopic);
        }
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

    public ResponseEntity<String> getDeviceDataByName(String deviceName) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        Device device = this.deviceRepository.getDeviceByName(deviceName);
        if (device != null && device.getData() != null) {
            responseBody.put("data", device.getData());
            responseBody.put("success", true);
        } else {
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
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

                this.addSubscription(updatedDevice.getDeviceName() + "/#");

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

        this.removeSubscription(device.getDeviceName() + "/#");

        this.deviceRepository.deleteById(device.getId());

        responseBody.put("success", true);
        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
