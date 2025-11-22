package com.smarthome.webapp.objects;

import java.time.Instant;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "device_readings")
public class DeviceReading {
    private ObjectId id;
    private Instant timestamp;
    private Map<String, Object> metadata;   // deviceName, type, sensorName, etc.
    private Object value;  // numeric or string

    public DeviceReading(Instant timestamp, Map<String, Object> metadata, Object value) {
        this.timestamp = timestamp;
        this.metadata = metadata;
        this.value = value;
    }
}

