package com.golgolengi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank(message = "accessToken은 필수입니다.") String accessToken
) {}
