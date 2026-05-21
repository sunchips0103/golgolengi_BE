package com.golgolengi.health.repository;

import com.golgolengi.health.domain.HealthProfile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HealthProfileRepository extends MongoRepository<HealthProfile, ObjectId> {
    Optional<HealthProfile> findByMemberId(String memberId);
    boolean existsByMemberId(String memberId);
}
