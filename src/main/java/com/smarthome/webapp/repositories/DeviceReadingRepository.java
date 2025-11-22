package com.smarthome.webapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smarthome.webapp.objects.DeviceReading;

public interface DeviceReadingRepository extends MongoRepository<DeviceReading, String> {

}
