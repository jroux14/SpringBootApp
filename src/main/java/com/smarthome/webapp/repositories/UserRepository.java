package com.smarthome.webapp.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smarthome.webapp.objects.User;

public interface UserRepository extends MongoRepository<User, String> {
    @Query("{ 'userID' : { $eq: ?0 } }")
    User findByUserID(String userID);

    @Query("{ 'username' : { $eq: ?0 } }")
    User findByUsername(String username);
}
