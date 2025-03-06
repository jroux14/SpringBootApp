package com.smarthome.webapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import com.smarthome.webapp.objects.Device;

public interface DeviceRepository extends MongoRepository<Device, String> {

    @Query("{ 'userId' : { $eq: ?0 } }")
    Device[] getDeviceByUserId(String userId);

    @Query("{ 'userId' : null }")
    Device[] getUnclaimedDevices();

    @Query("{ 'deviceName' : { $eq: ?0 } }")
    Device getDeviceByName(String deviceId);

    @Query("{ 'deviceName' : { $eq: ?0 } }")
    @Update("{ '$set': { 'deviceNameFriendly': ?1, 'item': ?2, 'userId': ?3 } }")
    void claimDevice(String deviceName, String friendlyName, Object item, String userId);

    @Query("{ 'deviceID' : { $eq: ?0 } }")
    Device getDeviceById(String deviceId);

    @Query(value="{ 'deviceID' : { $eq: ?0 } }", delete=true)
    Device deleteByDeviceId(String deviceId);
}