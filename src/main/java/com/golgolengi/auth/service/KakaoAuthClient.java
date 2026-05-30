package com.golgolengi.auth.service;

import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoAuthClient {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_ME_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", restApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(params, headers),
                    Map.class
            );
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_KAKAO_TOKEN);
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_USER_ME_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            return parseUserInfo(response.getBody());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_KAKAO_TOKEN);
        }
    }

    @SuppressWarnings("unchecked")
    private KakaoUserInfo parseUserInfo(Map<String, Object> body) {
        String kakaoId = String.valueOf(body.get("id"));
        Map<String, Object> account = (Map<String, Object>) body.get("kakao_account");
        String email = null;
        String name = null;
        String profileImageUrl = null;
        if (account != null) {
            email = (String) account.get("email");
            Map<String, Object> profile = (Map<String, Object>) account.get("profile");
            if (profile != null) {
                name = (String) profile.get("nickname");
                profileImageUrl = (String) profile.get("profile_image_url");
            }
        }
        return new KakaoUserInfo(kakaoId, email, name, profileImageUrl);
    }

    public record KakaoUserInfo(String id, String email, String name, String profileImageUrl) {}
}
