package com.golgolengi.family.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyMemberResponse {
    private String memberId;
    private String name;
    private String profileImageUrl;
    private String role;
    private boolean riskScoreVisible;
    private boolean missionVisible;
}