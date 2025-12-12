package com.smarthome.webapp.device.domain;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DeviceData {

    private final String status;
    private final Map<String, Object> sensors;
    private final Map<String, Object> switches;
    private final Map<String, Object> binarySensors;

    public DeviceData withUpdate(
        PeripheralType type,
        String name,
        Object value
    ) {
        return switch (type) {
            case SENSOR -> copyWith(sensors, name, value, switches, binarySensors);
            case SWITCH -> copyWith(switches, name, value, sensors, binarySensors);
            case BINARY_SENSOR -> copyWith(binarySensors, name, value, sensors, switches);
        };
    }

    private DeviceData copyWith(
        Map<String, Object> target,
        String key,
        Object value,
        Map<String, Object> other1,
        Map<String, Object> other2
    ) {
        Map<String, Object> updated = new HashMap<>();
        if (target != null) updated.putAll(target);
        updated.put(key, value);

        return DeviceData.builder()
            .status(status)
            .sensors(target == sensors ? updated : sensors)
            .switches(target == switches ? updated : switches)
            .binarySensors(target == binarySensors ? updated : binarySensors)
            .build();
    }
}
