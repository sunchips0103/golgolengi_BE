package com.golgolengi.mission.repository;

import com.golgolengi.mission.domain.Mission;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MissionRepository extends MongoRepository<Mission, ObjectId> {
    List<Mission> findByFamilyIdAndStatus(String familyId, String status);
    List<Mission> findByFamilyId(String familyId);
}
