package com.smarthome.webapp.aggregation.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DailyReadingRepository extends MongoRepository<DailyReadingDocument, String> {
    List<DailyReadingDocument> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
