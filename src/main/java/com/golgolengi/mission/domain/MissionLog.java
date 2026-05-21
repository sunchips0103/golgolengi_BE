package com.golgolengi.mission.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "mission_logs")
@CompoundIndex(name = "mission_member_idx", def = "{'missionId': 1, 'memberId': 1}")
public class MissionLog {

    @Id
    private ObjectId id;

    private String missionId;
    private String memberId;
    private int value;

    @CreatedDate
    private LocalDateTime checkedAt;
}
