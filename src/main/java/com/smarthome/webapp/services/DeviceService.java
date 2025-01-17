package com.smarthome.webapp.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.repositories.DeviceRepository;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public void saveDevice(Device device) {
        deviceRepository.save(device);
    }

    public Device[] getDevicesByUserId(String userId) {
        return this.deviceRepository.getByUserId(userId);
    }

    public Device getDeviceById(String deviceId) {
        return this.deviceRepository.getByDeviceId(deviceId);
    }

    public ResponseEntity<String> registerDevice(Device device) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if (device != null) {
                this.saveDevice(device);
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

        this.deviceRepository.deleteByDeviceId(device.getDeviceID());
        this.deviceRepository.save(device);

        responseBody.put("success", true);
        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> deleteDevice(Device device) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        this.deviceRepository.deleteByDeviceId(device.getDeviceID());

        responseBody.put("success", true);
        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
