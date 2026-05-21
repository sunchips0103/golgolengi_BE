package com.golgolengi.auth.controller;

import com.golgolengi.auth.dto.request.KakaoLoginRequest;
import com.golgolengi.auth.dto.response.TokenResponse;
import com.golgolengi.auth.service.AuthService;
import com.golgolengi.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoOAuthController {

    private final AuthService authService;

    @PostMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoCallback(
            @RequestBody @Valid KakaoLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithKakao(request)));
    }
}
