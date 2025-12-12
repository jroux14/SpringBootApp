package com.smarthome.webapp.aggregation.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface YearlyReadingRepository extends MongoRepository<YearlyReadingDocument, String> {
    List<YearlyReadingDocument> findByDeviceAndSensorAndTimestampBetween(String device, String sensor, Instant start, Instant end);
}
