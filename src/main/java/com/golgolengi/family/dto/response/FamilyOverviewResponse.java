package com.golgolengi.family.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FamilyOverviewResponse {
    private String familyId;
    private String name;
    private int memberCount;
    private List<FamilyMemberResponse> members;
}