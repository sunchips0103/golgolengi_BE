package com.golgolengi.family.domain;

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
@Document(collection = "family_members")
@CompoundIndex(name = "family_member_unique", def = "{'familyId': 1, 'memberId': 1}", unique = true)
public class FamilyMember {

    @Id
    private ObjectId id;

    private String familyId;
    private String memberId;

    @Builder.Default
    private String role = "MEMBER"; // "OWNER" | "MEMBER"

    @Builder.Default
    private PrivacySetting privacySetting = PrivacySetting.defaultSetting();

    @CreatedDate
    private LocalDateTime joinedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivacySetting {
        @Builder.Default
        private boolean riskScoreVisible = true;
        @Builder.Default
        private boolean missionVisible = true;

        public static PrivacySetting defaultSetting() {
            return new PrivacySetting(true, true);
        }
    }
}