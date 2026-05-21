package com.golgolengi.auth.service;

import com.golgolengi.auth.dto.response.TokenResponse;
import com.golgolengi.global.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        TokenResponse token = authService.loginWithGoogle(oAuth2User);

        // 앱 딥링크로 토큰 전달 → expo-auth-session이 result.url로 수신
        String redirectUri = UriComponentsBuilder
                .fromUriString(appProperties.getOauthRedirectUri())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("refresh_token", token.getRefreshToken())
                .queryParam("is_new_member", token.isNewMember())
                .build().toUriString();

        response.sendRedirect(redirectUri);
    }
}