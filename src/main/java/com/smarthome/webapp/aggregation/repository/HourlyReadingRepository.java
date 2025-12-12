package com.smarthome.webapp.aggregation.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface HourlyReadingRepository extends MongoRepository<HourlyReadingDocument, String> {
    List<HourlyReadingDocument> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
