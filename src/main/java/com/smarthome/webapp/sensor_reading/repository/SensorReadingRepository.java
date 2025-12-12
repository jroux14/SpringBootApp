package com.smarthome.webapp.sensor_reading.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SensorReadingRepository extends MongoRepository<SensorReadingDocument, String> {
    @Query(
        value = "{ 'metadata.device': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }",
        sort = "{ 'timestamp': 1 }"
    )
    List<SensorReadingDocument> findReadingsByDeviceId(String deviceId, Instant start, Instant end);
}
