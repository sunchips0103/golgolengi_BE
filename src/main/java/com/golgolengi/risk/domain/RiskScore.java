package com.golgolengi.risk.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "risk_scores")
@CompoundIndex(def = "{'memberId': 1, 'calculatedAt': -1}")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScore {

    @Id
    private ObjectId id;

    private String memberId;
    private double totalScore;

    private double geneticScore;
    private double lifestyleScore;
    private double behaviorScore;
    private double environmentScore;
    private double clinicalScore;

    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
