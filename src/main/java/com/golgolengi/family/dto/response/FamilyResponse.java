package com.golgolengi.family.dto.response;

import com.golgolengi.family.domain.Family;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyResponse {
    private String familyId;
    private String name;
    private String inviteCode;
    private String createdByMemberId;

    public static FamilyResponse from(Family family) {
        return FamilyResponse.builder()
                .familyId(family.getId().toHexString())
                .name(family.getName())
                .inviteCode(family.getInviteCode())
                .createdByMemberId(family.getCreatedByMemberId())
                .build();
    }
}