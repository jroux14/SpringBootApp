package com.smarthome.webapp.device.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Device {
    private final String id;
    private final String deviceType;

    private String userId;
    private String deviceName;
    private String deviceNameFriendly;
    private String roomId;
    private DeviceData data;

    @Builder
    private Device(String id, String deviceType, String userId, String deviceName, String deviceNameFriendly, String roomId, DeviceData data) {
        this.id = id;
        this.deviceType = deviceType;
        this.userId = userId;
        this.deviceName = deviceName;
        this.deviceNameFriendly = deviceNameFriendly;
        this.roomId = roomId;
        this.data = data;
    }

    public void claim(String userId) {
        if (this.userId != null) {
            throw new IllegalStateException("Device already claimed");
        }
        this.userId = userId;
    }

    public void assignRoom(String roomId) {
        if (userId == null) {
            throw new IllegalStateException("Unclaimed device cannot be assigned to a room");
        }
        this.roomId = roomId;
    }

    public void updateData(DeviceData data) {
        this.data = data;
    }

    public void setDeviceNameFriendly(String deviceNameFriendly) {
        this.deviceNameFriendly = deviceNameFriendly;
    }
}
