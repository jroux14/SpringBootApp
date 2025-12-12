package com.smarthome.webapp.device.domain;

import java.util.Optional;

import com.smarthome.webapp.constants.DeviceConstants;

public enum PeripheralType {
    SENSOR,
    SWITCH,
    BINARY_SENSOR;

    public static Optional<PeripheralType> resolve(String rawType, String name) {
        return switch (rawType) {
            case "sensor" ->
                DeviceConstants.VALID_OUTLET_SENSORS.contains(name)
                    ? Optional.of(SENSOR)
                    : Optional.empty();
            case "switch" -> Optional.of(SWITCH);
            case "binary_sensor" -> Optional.of(BINARY_SENSOR);
            default -> Optional.empty();
        };
    }
}