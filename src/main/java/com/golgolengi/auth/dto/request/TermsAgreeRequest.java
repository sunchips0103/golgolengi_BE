package com.golgolengi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TermsAgreeRequest {

    @NotBlank(message = "socialProvider는 필수입니다.")
    private String socialProvider;

    @NotBlank(message = "socialId는 필수입니다.")
    private String socialId;

    private boolean marketingAgreed;
}