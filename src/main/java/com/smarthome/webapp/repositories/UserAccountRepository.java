package com.smarthome.webapp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.smarthome.webapp.objects.UserAccount;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
    @Query("{ 'username' : { $eq: ?0 } }")
    UserAccount findByUsername(String username);
}
