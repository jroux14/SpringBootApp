package com.smarthome.webapp.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smarthome.webapp.objects.HourlyReading;

public interface HourlyReadingRepository extends MongoRepository<HourlyReading, String> {
    List<HourlyReading> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
