package com.smarthome.webapp.objects;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document("device_readings_monthly")
public class MonthlyReading {
    @Id
    private String id;

    private double avg;
    private double min;
    private double max;
    private long count;

    private String device;
    private String sensor;

    private Instant timestamp;
}
