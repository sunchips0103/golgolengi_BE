package com.golgolengi.member.repository;

import com.golgolengi.member.domain.Member;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, ObjectId> {
    Optional<Member> findBySocialProviderAndSocialId(String socialProvider, String socialId);
    Optional<Member> findById(ObjectId id);
}