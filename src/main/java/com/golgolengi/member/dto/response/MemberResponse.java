package com.golgolengi.member.dto.response;

import com.golgolengi.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private String memberId;
    private String name;
    private String email;
    private String profileImageUrl;
    private boolean onboardingCompleted;
    private String familyId;

    public static MemberResponse of(Member member, String familyId) {
        return MemberResponse.builder()
                .memberId(member.getId().toHexString())
                .name(member.getName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .onboardingCompleted(member.isOnboardingCompleted())
                .familyId(familyId)
                .build();
    }
}