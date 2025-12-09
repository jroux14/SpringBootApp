package com.smarthome.webapp.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smarthome.webapp.objects.DailyReading;

public interface DailyReadingRepository extends MongoRepository<DailyReading, String> {
    List<DailyReading> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
