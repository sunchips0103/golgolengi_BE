package com.golgolengi.auth.repository;

import com.golgolengi.auth.domain.LogoutToken;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogoutTokenRepository extends MongoRepository<LogoutToken, ObjectId> {
    boolean existsByToken(String token);
}