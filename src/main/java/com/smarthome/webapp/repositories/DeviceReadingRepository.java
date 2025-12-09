package com.smarthome.webapp.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smarthome.webapp.objects.DeviceReading;

public interface DeviceReadingRepository extends MongoRepository<DeviceReading, String> {
    @Query(
        value = "{ 'metadata.device': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }",
        sort = "{ 'timestamp': 1 }"
    )
    List<DeviceReading> findReadingsByDeviceId(String deviceId, Instant start, Instant end);
}
