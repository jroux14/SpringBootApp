package com.smarthome.webapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smarthome.webapp.objects.UserInfo;

public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    @Query("{ 'firstName' : { $eq: ?0 } }")
    UserInfo findByName(String firstName);

    @Query("{ 'userId' : { $eq: ?0 } }")
    UserInfo findByUserId(String userId);
}
