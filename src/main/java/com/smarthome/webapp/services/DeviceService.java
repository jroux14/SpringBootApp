package com.smarthome.webapp.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<HashMap<String,Object>> registerDevice(HashMap<String,Object> device) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        try {
            if (device != null) {

                Device newDevice = Device.builder()
                .deviceID(device.get("deviceID").toString())
                .userId(device.get("userID").toString())
                .deviceType(device.get("deviceType").toString())
                .deviceName(device.get("deviceName").toString())
                .displayWidth((int)device.get("displayWidth"))
                .displayHeight((int)device.get("displayHeight"))
                .posX((int)device.get("posX"))
                .posY((int)device.get("posY"))
                .item(device.get("item")).build();

                this.saveDevice(newDevice);

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

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    /* TODO: Flesh this method out some more */
    public ResponseEntity<HashMap<String,Object>> updateItem(String deviceId, Object item) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        // System.out.println(item);
        Device device = getDeviceById(deviceId);
        this.deviceRepository.deleteByDeviceId(deviceId);
        device.setItem(item);

        this.deviceRepository.save(device);

        responseBody.put("success", true);

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }
}
