package com.golgolengi.health.dto.response;

import com.golgolengi.health.domain.HealthProfile;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HealthProfileResponse {
    private String profileId;
    private String memberId;
    private List<String> conditions;
    private List<FamilyHistoryItemResponse> familyHistory;
    private LifestyleResponse lifestyle;

    @Getter
    @Builder
    public static class FamilyHistoryItemResponse {
        private String relation;
        private String condition;
    }

    @Getter
    @Builder
    public static class LifestyleResponse {
        private boolean smoker;
        private String drinkingFrequency;
        private String exerciseFrequency;
        private String dietQuality;
        private int sleepHours;
    }

    public static HealthProfileResponse from(HealthProfile profile) {
        LifestyleResponse lifestyle = null;
        if (profile.getLifestyle() != null) {
            HealthProfile.Lifestyle ls = profile.getLifestyle();
            lifestyle = LifestyleResponse.builder()
                    .smoker(ls.isSmoker())
                    .drinkingFrequency(ls.getDrinkingFrequency())
                    .exerciseFrequency(ls.getExerciseFrequency())
                    .dietQuality(ls.getDietQuality())
                    .sleepHours(ls.getSleepHours())
                    .build();
        }
        List<FamilyHistoryItemResponse> familyHistory = profile.getFamilyHistory().stream()
                .map(fh -> FamilyHistoryItemResponse.builder()
                        .relation(fh.getRelation())
                        .condition(fh.getCondition())
                        .build())
                .toList();
        return HealthProfileResponse.builder()
                .profileId(profile.getId().toHexString())
                .memberId(profile.getMemberId())
                .conditions(profile.getConditions())
                .familyHistory(familyHistory)
                .lifestyle(lifestyle)
                .build();
    }
}
