package com.golgolengi.auth.service;

import com.golgolengi.auth.domain.Consent;
import com.golgolengi.auth.domain.LogoutToken;
import com.golgolengi.auth.dto.request.TermsAgreeRequest;
import com.golgolengi.auth.dto.response.TokenResponse;
import com.golgolengi.auth.repository.ConsentRepository;
import com.golgolengi.auth.repository.LogoutTokenRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.global.jwt.JwtProvider;
import com.golgolengi.member.domain.Member;
import com.golgolengi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final ConsentRepository consentRepository;
    private final LogoutTokenRepository logoutTokenRepository;
    private final JwtProvider jwtProvider;

    public TokenResponse loginWithGoogle(OAuth2User oAuth2User) {
        String socialId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        boolean isNew = !memberRepository.findBySocialProviderAndSocialId("google", socialId).isPresent();

        Member member = memberRepository.findBySocialProviderAndSocialId("google", socialId)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .socialProvider("google")
                        .socialId(socialId)
                        .email(email)
                        .name(name)
                        .profileImageUrl(picture)
                        .build()));

        String memberId = member.getId().toHexString();
        return TokenResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(memberId))
                .refreshToken(jwtProvider.generateRefreshToken(memberId))
                .isNewMember(isNew)
                .build();
    }

    public void agreeTerms(TermsAgreeRequest request) {
        Member member = memberRepository.findBySocialProviderAndSocialId(
                request.getSocialProvider(), request.getSocialId()
        ).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String memberId = member.getId().toHexString();
        if (!consentRepository.existsByMemberId(memberId)) {
            consentRepository.save(Consent.builder()
                    .memberId(memberId)
                    .termsAgreed(true)
                    .privacyAgreed(true)
                    .marketingAgreed(request.isMarketingAgreed())
                    .build());
        }
    }

    public TokenResponse refresh(String refreshToken) {
        jwtProvider.validate(refreshToken);

        if (logoutTokenRepository.existsByToken(refreshToken)) {
            throw new CustomException(ErrorCode.LOGOUT_TOKEN);
        }

        String memberId = jwtProvider.extractMemberId(refreshToken);
        return TokenResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(memberId))
                .refreshToken(jwtProvider.generateRefreshToken(memberId))
                .isNewMember(false)
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        blacklist(accessToken);
        blacklist(refreshToken);
    }

    private void blacklist(String token) {
        LocalDateTime expiresAt = jwtProvider.getExpiration(token)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        logoutTokenRepository.save(LogoutToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build());
    }
}