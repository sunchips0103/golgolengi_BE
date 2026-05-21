package com.golgolengi.mission.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "badges")
@CompoundIndex(name = "member_badge_unique", def = "{'memberId': 1, 'badgeType': 1}", unique = true)
public class Badge {

    @Id
    private ObjectId id;

    private String memberId;
    private String badgeType;  // FIRST_CHECKIN, STREAK_3, STREAK_7, STREAK_30
    private LocalDateTime awardedAt;
}
