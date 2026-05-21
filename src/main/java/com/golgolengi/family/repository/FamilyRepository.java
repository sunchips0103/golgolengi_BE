package com.golgolengi.family.repository;

import com.golgolengi.family.domain.Family;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FamilyRepository extends MongoRepository<Family, ObjectId> {
    Optional<Family> findByInviteCode(String inviteCode);
}