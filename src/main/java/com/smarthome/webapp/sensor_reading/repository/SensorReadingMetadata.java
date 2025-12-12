package com.smarthome.webapp.sensor_reading.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SensorReadingMetadata {
    private String device;
    private String name;
    private String type;
}

