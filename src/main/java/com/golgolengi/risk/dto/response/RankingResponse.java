package com.golgolengi.risk.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankingResponse {
    private int rank;
    private String memberId;
    private String name;
    private String profileImageUrl;
    private double totalScore;
    private String riskLevel;
}
