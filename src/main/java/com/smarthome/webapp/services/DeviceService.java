package com.smarthome.webapp.services;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.objects.DeviceReading;
import com.smarthome.webapp.objects.DeviceReadingMetadata;
import com.smarthome.webapp.repositories.DeviceReadingRepository;
import com.smarthome.webapp.repositories.DeviceRepository;
import com.smarthome.webapp.DeviceConstants;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceReadingRepository deviceReadingRepository;

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
                                        if (Arrays.asList(DeviceConstants.VALID_OUTLET_SENSORS).contains(topicArr[2])) {
                                            JsonNode sensorNode = dataNode.get("sensors");
                                            if (sensorNode == null || !sensorNode.isObject()) {
                                                sensorNode = objectMapper.createObjectNode();
                                            }
                                            ObjectNode sensorObj = (ObjectNode) sensorNode;
                                            String sensorName = topicArr[2];
                                            String sensorData = payloadObj.toString();
                                            sensorData = sensorData.trim();
                                            Object value;
                                            try {
                                                value = Double.valueOf(sensorData);
                                            } catch (NumberFormatException e) {
                                                value = sensorData;
                                            }
                                            
                                            JsonNode node = objectMapper.convertValue(value, JsonNode.class);
    
                                            // Update live state
                                            sensorObj.set(sensorName, node);
                                            dataObj.set("sensors", sensorObj);
    
                                            ObjectId deviceId = new ObjectId(device.getId());
                                            this.deviceRepository.updateDeviceData(deviceId, objectMapper.treeToValue(dataObj, Object.class));
                                        
                                            // Store historical data
                                            saveReading(device.getId(), "sensor", sensorName, value);
                                        }
                                    } else if (topicArr[1].equals("binary_sensor")) {
                                        JsonNode binarySensorNode = dataNode.get("binarySensors");
                                        if (binarySensorNode == null || !binarySensorNode.isObject()) {
                                            binarySensorNode = objectMapper.createObjectNode();
                                        }
                                        ObjectNode binarySensorObj = (ObjectNode) binarySensorNode;
                                        String binarySensorName = topicArr[2];
                                        String binarySensorData = payloadObj.toString();
                                        binarySensorData = binarySensorData.trim();
                                        Object value;
                                        try {
                                            value = Double.valueOf(binarySensorData);
                                        } catch (NumberFormatException e) {
                                            value = binarySensorData;
                                        }
                                        JsonNode node = objectMapper.convertValue(value, JsonNode.class);

                                        // Update live state
                                        binarySensorObj.set(binarySensorName, node);
                                        dataObj.set("binarySensors", binarySensorObj);

                                        ObjectId deviceId = new ObjectId(device.getId());
                                        this.deviceRepository.updateDeviceData(deviceId, objectMapper.treeToValue(dataObj, Object.class));                                    
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

                                        ObjectId deviceId = new ObjectId(device.getId());
                                        this.deviceRepository.updateDeviceData(deviceId, objectMapper.treeToValue(dataObj, Object.class));
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

    private void saveReading(String deviceId, String type, String name, Object value) {   
        DeviceReadingMetadata metadata = new DeviceReadingMetadata(deviceId, name, type); 
        DeviceReading reading = new DeviceReading(Instant.now(), metadata, value);
    
        deviceReadingRepository.save(reading);
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

    public Device getDeviceById(String idStr) {
        ObjectId deviceId = new ObjectId(idStr);
        return this.deviceRepository.getDeviceById(deviceId);
    }

    public ResponseEntity<String> getUserDeviceData(String userId) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        Device[] devices = this.deviceRepository.getDeviceByUserId(userId);
        if (devices != null) {
            ObjectNode deviceNode = objectMapper.createObjectNode();
            for (Device device : devices) {
                Object deviceData = device.getData();
                if (deviceData != null) {
                    JsonNode deviceDataJson = objectMapper.valueToTree(deviceData);
                    deviceNode.set(device.getId(), deviceDataJson);
                }
            }

            responseBody.put("data", deviceNode);
            responseBody.put("success", true);
        } else {
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> getDeviceDataById(String idStr) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectId deviceId = new ObjectId(idStr);
        Device device = this.deviceRepository.getDeviceById(deviceId);
        if (device != null) {
            ObjectNode deviceNode = objectMapper.createObjectNode();
            Object deviceData = device.getData();
            if (deviceData != null) {
                JsonNode deviceDataJson = objectMapper.valueToTree(deviceData);
                deviceNode.set("data", deviceDataJson);
            }

            responseBody.put("data", deviceNode);
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
                Device updatedDevice = device;

                // Temporary conditional while developing
                if (device.getDeviceName().contains("dev-")) {
                    this.deviceRepository.save(device);
                } else {
                    this.deviceRepository.claimDevice(device.getDeviceName(), device.getDeviceNameFriendly(), device.getUserId(), device.getRoomId());
                    updatedDevice = this.deviceRepository.getDeviceByName(device.getDeviceName());
    
                    this.addSubscription(updatedDevice.getDeviceName() + "/#");
                }
                
                responseBody.put("id", updatedDevice.getId());
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No Device");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Unkown server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> getSensorReadingsByDeviceId(String deviceId, Instant start, Instant end) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        try {
            List<DeviceReading> deviceReadings = deviceReadingRepository.findReadingsByDeviceId(deviceId, start, end);

            if (deviceReadings != null) {
                responseBody.put("data", deviceReadings);
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "No sensor data");
                responseBody.put("success", false);
            }
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Failed to fetch sensor data");
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
