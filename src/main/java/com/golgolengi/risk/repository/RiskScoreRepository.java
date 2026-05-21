package com.golgolengi.risk.repository;

import com.golgolengi.risk.domain.RiskScore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RiskScoreRepository extends MongoRepository<RiskScore, ObjectId> {
    Optional<RiskScore> findTopByMemberIdOrderByCalculatedAtDesc(String memberId);
    List<RiskScore> findByMemberIdOrderByCalculatedAtDesc(String memberId);
}
