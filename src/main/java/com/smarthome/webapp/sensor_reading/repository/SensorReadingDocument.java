package com.smarthome.webapp.sensor_reading.repository;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;

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
@Document(collection = "device_readings")
@TimeSeries(timeField = "timestamp", metaField = "metadata")
public class SensorReadingDocument {

    @Id
    private String id;

    private Instant timestamp;
    private SensorReadingMetadata metadata;
    private Object value;  // numeric or string

    public SensorReadingDocument(Instant timestamp, SensorReadingMetadata metadata, Object value) {
        this.timestamp = timestamp;
        this.metadata = metadata;
        this.value = value;
    }
}

