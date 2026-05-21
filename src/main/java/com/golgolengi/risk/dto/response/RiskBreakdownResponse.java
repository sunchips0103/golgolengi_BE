package com.golgolengi.risk.dto.response;

import com.golgolengi.risk.domain.RiskScore;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RiskBreakdownResponse {
    private String scoreId;
    private double totalScore;
    private double geneticScore;
    private double lifestyleScore;
    private double behaviorScore;
    private double environmentScore;
    private double clinicalScore;
    private LocalDateTime calculatedAt;

    public static RiskBreakdownResponse from(RiskScore rs) {
        return RiskBreakdownResponse.builder()
                .scoreId(rs.getId().toHexString())
                .totalScore(rs.getTotalScore())
                .geneticScore(rs.getGeneticScore())
                .lifestyleScore(rs.getLifestyleScore())
                .behaviorScore(rs.getBehaviorScore())
                .environmentScore(rs.getEnvironmentScore())
                .clinicalScore(rs.getClinicalScore())
                .calculatedAt(rs.getCalculatedAt())
                .build();
    }
}
