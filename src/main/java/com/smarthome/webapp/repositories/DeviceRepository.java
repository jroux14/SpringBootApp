package com.smarthome.webapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.smarthome.webapp.objects.Device;

public interface DeviceRepository extends MongoRepository<Device, String> {

}