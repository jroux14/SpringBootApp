package com.smarthome.webapp.device.domain.exception;

public class UnauthorizedDeviceAccessException extends RuntimeException {
    public UnauthorizedDeviceAccessException(String deviceId) {
        super("User does not have permission to access device: " + deviceId);
    }
}
