package com.golgolengi.auth.controller;

import com.golgolengi.auth.dto.request.AppleCallbackRequest;
import com.golgolengi.auth.dto.response.TokenResponse;
import com.golgolengi.auth.service.AuthService;
import com.golgolengi.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final AuthService authService;

    // Apple: 앱이 네이티브 SDK로 id_token 취득 후 JSON 전달
    @PostMapping("/oauth/apple/callback")
    public ResponseEntity<ApiResponse<TokenResponse>> appleCallback(
            @RequestBody AppleCallbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginWithApple(request)));
    }
}