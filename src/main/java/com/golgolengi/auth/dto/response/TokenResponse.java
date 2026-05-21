package com.golgolengi.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isNewMember;
    private boolean onboardingCompleted;
}