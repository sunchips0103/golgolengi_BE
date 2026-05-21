package com.golgolengi.family.repository;

import com.golgolengi.family.domain.FamilyMember;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends MongoRepository<FamilyMember, ObjectId> {
    Optional<FamilyMember> findByMemberId(String memberId);
    List<FamilyMember> findByFamilyId(String familyId);
    boolean existsByMemberId(String memberId);
    Optional<FamilyMember> findByFamilyIdAndMemberId(String familyId, String memberId);
    void deleteByFamilyIdAndMemberId(String familyId, String memberId);
}