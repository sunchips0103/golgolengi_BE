package com.golgolengi.risk.dto.response;

import com.golgolengi.risk.domain.RiskScore;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RiskScoreResponse {
    private String scoreId;
    private String memberId;
    private double totalScore;
    private LocalDateTime calculatedAt;

    public static RiskScoreResponse from(RiskScore rs) {
        return RiskScoreResponse.builder()
                .scoreId(rs.getId().toHexString())
                .memberId(rs.getMemberId())
                .totalScore(rs.getTotalScore())
                .calculatedAt(rs.getCalculatedAt())
                .build();
    }
}
