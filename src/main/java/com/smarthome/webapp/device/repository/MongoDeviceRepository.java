package com.smarthome.webapp.device.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.device.domain.Device;
import com.smarthome.webapp.device.domain.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoDeviceRepository implements DeviceRepository {

    private final SpringDeviceRepository repo;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Device> findById(String id) {
        return repo.findById(id)
            .map(DeviceMapper::toDomain);
    }

    @Override
    public Optional<Device> findByName(String name) {
        return repo.findByDeviceName(name)
            .map(DeviceMapper::toDomain);
    }

    @Override
    public List<Device> findByUserId(String userId) {
        return repo.findByUserId(userId).stream()
            .map(DeviceMapper::toDomain)
            .toList();
    }

    @Override
    public List<Device> findUnclaimed() {
        return repo.findByUserIdIsNull().stream()
            .map(DeviceMapper::toDomain)
            .toList();
    }

    @Override
    public Device save(Device device) {
        DeviceDocument doc = DeviceMapper.toDocument(device, objectMapper);
        return DeviceMapper.toDomain(repo.save(doc));
    }

    @Override
    public void deleteById(String id) {
        repo.deleteById(id);
    }
}


