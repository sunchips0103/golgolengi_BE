package com.golgolengi.risk.dto.response;

import com.golgolengi.risk.domain.RiskScore;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberRiskSummaryResponse {
    private String memberId;
    private double totalScore;
    private String riskLevel;
    private LocalDateTime calculatedAt;

    public static MemberRiskSummaryResponse from(String memberId, RiskScore rs) {
        return MemberRiskSummaryResponse.builder()
                .memberId(memberId)
                .totalScore(rs.getTotalScore())
                .riskLevel(toLevel(rs.getTotalScore()))
                .calculatedAt(rs.getCalculatedAt())
                .build();
    }

    public static String calcRiskLevel(double score) {
        return toLevel(score);
    }

    private static String toLevel(double score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}
