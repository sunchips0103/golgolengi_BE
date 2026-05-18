package com.golgolengi.auth.repository;

import com.golgolengi.auth.domain.Consent;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConsentRepository extends MongoRepository<Consent, ObjectId> {
    Optional<Consent> findByMemberId(String memberId);
    boolean existsByMemberId(String memberId);
}