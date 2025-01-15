package com.smarthome.webapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smarthome.webapp.objects.Device;

public interface DeviceRepository extends MongoRepository<Device, String> {
    @Query("{ 'userId' : { $eq: ?0 } }")
    Device[] getByUserId(String userId);

    @Query("{ 'deviceID' : { $eq: ?0 } }")
    Device getByDeviceId(String deviceId);

    @Query(value="{ 'deviceID' : { $eq: ?0 } }", delete=true)
    Device deleteByDeviceId(String deviceId);
}