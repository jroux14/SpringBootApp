package com.smarthome.webapp.repositories;

import org.bson.types.ObjectId;
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
    @Update("{ '$set': { 'deviceNameFriendly': ?1, 'userId': ?2, 'roomId': ?3 } }")
    void claimDevice(String deviceName, String friendlyName, String userId, String roomId);

    @Query("{ 'deviceName' : { $eq: ?0 } }")
    @Update("{ '$set': { 'data': ?1 } }")
    void updateDeviceData(String deviceName, Object data);

    @Query("{ '_id' : { $eq: ?0 } }")
    Device getDeviceById(ObjectId deviceId);

    @Query(value="{ '_id' : { $eq: ?0 } }", delete=true)
    Device deleteByDeviceId(ObjectId deviceId);
}