package com.golgolengi.health.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "health_profiles")
public class HealthProfile {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String memberId;

    @Builder.Default
    private List<String> conditions = new ArrayList<>();

    @Builder.Default
    private List<FamilyHistoryItem> familyHistory = new ArrayList<>();

    private Lifestyle lifestyle;

    @Builder.Default
    private List<HealthDataPoint> healthData = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── 내장 타입 ──────────────────────────────────────────────────────────────

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyHistoryItem {
        private String relation;   // FATHER, MOTHER, SIBLING, GRANDPARENT
        private String condition;  // HYPERTENSION, DIABETES, HEART_DISEASE, CANCER, STROKE
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Lifestyle {
        private boolean smoker;
        private String drinkingFrequency;  // NONE, OCCASIONAL, FREQUENT
        private String exerciseFrequency;  // NONE, ONCE_WEEK, TWO_THREE_WEEK, DAILY
        private String dietQuality;        // POOR, FAIR, GOOD
        private int sleepHours;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthDataPoint {
        private String type;   // STEPS, HEART_RATE, WEIGHT, BLOOD_PRESSURE_SYSTOLIC
        private double value;
        private String unit;
        private LocalDateTime recordedAt;
    }
}
