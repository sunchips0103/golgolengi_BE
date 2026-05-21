package com.golgolengi.auth.controller;

import com.golgolengi.auth.dto.request.TermsAgreeRequest;
import com.golgolengi.auth.dto.request.TokenRefreshRequest;
import com.golgolengi.auth.dto.response.TokenResponse;
import com.golgolengi.auth.service.AuthService;
import com.golgolengi.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/terms")
    public ResponseEntity<ApiResponse<Void>> agreeTerms(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid TermsAgreeRequest request) {
        authService.agreeTerms(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("약관 동의가 완료되었습니다."));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody @Valid TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request.getRefreshToken())));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody @Valid TokenRefreshRequest request) {
        String accessToken = bearerToken.substring(7);
        authService.logout(accessToken, request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("로그아웃되었습니다."));
    }
}
