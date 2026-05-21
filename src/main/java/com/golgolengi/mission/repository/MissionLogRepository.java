package com.golgolengi.mission.repository;

import com.golgolengi.mission.domain.MissionLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MissionLogRepository extends MongoRepository<MissionLog, ObjectId> {
    List<MissionLog> findByMissionId(String missionId);
    List<MissionLog> findByMemberId(String memberId);
    List<MissionLog> findByMemberIdAndCheckedAtBetween(String memberId, LocalDateTime from, LocalDateTime to);
    long countByMemberId(String memberId);
    List<MissionLog> findByMemberIdOrderByCheckedAtDesc(String memberId);
}
