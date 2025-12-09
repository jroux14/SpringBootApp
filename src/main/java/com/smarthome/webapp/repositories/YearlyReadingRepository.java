package com.smarthome.webapp.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smarthome.webapp.objects.YearlyReading;

public interface YearlyReadingRepository extends MongoRepository<YearlyReading, String> {
    List<YearlyReading> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
