package com.smarthome.webapp.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AuthAccountRepository extends MongoRepository<AuthAccountDocument, String> {
    @Query("{ 'username' : { $eq: ?0 } }")
    AuthAccountDocument findByUsername(String username);

    @Query("{ 'refreshToken' : { $eq: ?0 } }")
    AuthAccountDocument findByRefreshToken(String refreshToken);
}
