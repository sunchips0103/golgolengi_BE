package com.golgolengi.mission.repository;

import com.golgolengi.mission.domain.Badge;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BadgeRepository extends MongoRepository<Badge, ObjectId> {
    List<Badge> findByMemberId(String memberId);
    boolean existsByMemberIdAndBadgeType(String memberId, String badgeType);
}
