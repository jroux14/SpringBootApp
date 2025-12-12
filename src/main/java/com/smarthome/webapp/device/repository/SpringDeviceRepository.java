package com.smarthome.webapp.device.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

interface SpringDeviceRepository
        extends MongoRepository<DeviceDocument, String> {

    List<DeviceDocument> findByUserId(String userId);

    List<DeviceDocument> findByUserIdIsNull();

    Optional<DeviceDocument> findByDeviceName(String deviceName);
}
