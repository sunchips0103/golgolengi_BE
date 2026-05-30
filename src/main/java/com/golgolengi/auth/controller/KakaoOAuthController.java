package com.golgolengi.auth.controller;

import com.golgolengi.auth.dto.request.KakaoLoginRequest;
import com.golgolengi.auth.dto.response.TokenResponse;
import com.golgolengi.auth.service.AuthService;
import com.golgolengi.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class KakaoOAuthController {

    private final AuthService authService;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @GetMapping("/oauth/kakao/login")
    public ResponseEntity<Void> kakaoLogin() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + restApiKey
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
        return ResponseEntity.status(302).location(URI.create(kakaoAuthUrl)).build();
    }

    // 카카오 리다이렉트 콜백 (code → JWT 발급)
    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoCodeCallback(
            @RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithKakaoCode(code)));
    }

    // 모바일 SDK용 (accessToken 직접 전달)
    @PostMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoCallback(
            @RequestBody @Valid KakaoLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithKakao(request)));
    }
}
