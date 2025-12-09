package com.smarthome.webapp.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smarthome.webapp.objects.MonthlyReading;

public interface MonthlyReadingRepository extends MongoRepository<MonthlyReading, String> {
    List<MonthlyReading> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
