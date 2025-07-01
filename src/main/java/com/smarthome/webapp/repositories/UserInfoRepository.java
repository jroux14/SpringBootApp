package com.smarthome.webapp.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import com.smarthome.webapp.objects.UserInfo;

public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    @Query("{ 'firstName' : { $eq: ?0 } }")
    UserInfo findByName(String firstName);

    @Query("{ 'userId' : { $eq: ?0 } }")
    UserInfo findByUserId(String userId);

    @Query(value = "{ 'userId': ?0, 'rooms.name': ?1 }", exists = true)
    boolean roomExistsForUser(String userId, String roomName);

    @Query("{ 'userId' : { $eq: ?0 } }")
    @Update("{ '$push': { 'rooms': ?1 } }")
    void addRoom(String userId, Object roomName);
}
