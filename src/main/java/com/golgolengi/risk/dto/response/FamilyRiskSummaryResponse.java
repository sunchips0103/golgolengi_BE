package com.golgolengi.risk.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FamilyRiskSummaryResponse {
    private String familyId;
    private double averageScore;
    private List<MemberRiskSummaryResponse> members;
}
