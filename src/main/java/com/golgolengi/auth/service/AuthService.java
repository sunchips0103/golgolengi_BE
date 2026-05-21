package com.golgolengi.auth.service;

import com.golgolengi.auth.domain.Consent;
import com.golgolengi.auth.domain.LogoutToken;
import com.golgolengi.auth.dto.AppleClaims;
import com.golgolengi.auth.dto.request.AppleCallbackRequest;
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
import org.bson.types.ObjectId;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final ConsentRepository consentRepository;
    private final LogoutTokenRepository logoutTokenRepository;
    private final JwtProvider jwtProvider;
    private final AppleIdTokenValidator appleIdTokenValidator;

    public TokenResponse loginWithGoogle(OAuth2User oAuth2User) {
        return findOrCreateAndIssue(
                "google",
                oAuth2User.getAttribute("sub"),
                oAuth2User.getAttribute("email"),
                oAuth2User.getAttribute("name"),
                oAuth2User.getAttribute("picture")
        );
    }

    public TokenResponse loginWithApple(AppleCallbackRequest request) {
        AppleClaims claims = appleIdTokenValidator.validate(request.idToken());
        String email = claims.email() != null ? claims.email()
                : (request.user() != null ? request.user().email() : null);
        String name = request.user() != null ? request.user().name() : null;
        return findOrCreateAndIssue("apple", claims.sub(), email, name, null);
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
        Member member = memberRepository.findById(new ObjectId(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return buildTokenResponse(memberId, false, member.isOnboardingCompleted());
    }

    public void logout(String accessToken, String refreshToken) {
        blacklist(accessToken);
        blacklist(refreshToken);
    }

    // ── 공통 내부 메서드 ──────────────────────────────────────────────────────

    private TokenResponse findOrCreateAndIssue(
            String provider, String socialId, String email, String name, String profileImageUrl) {
        Optional<Member> existing = memberRepository.findBySocialProviderAndSocialId(provider, socialId);
        boolean isNew = existing.isEmpty();
        Member member = existing.orElseGet(() -> memberRepository.save(Member.builder()
                .socialProvider(provider)
                .socialId(socialId)
                .email(email)
                .name(name)
                .profileImageUrl(profileImageUrl)
                .build()));
        return buildTokenResponse(member.getId().toHexString(), isNew, member.isOnboardingCompleted());
    }

    private TokenResponse buildTokenResponse(String memberId, boolean isNewMember, boolean onboardingCompleted) {
        return TokenResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(memberId))
                .refreshToken(jwtProvider.generateRefreshToken(memberId))
                .isNewMember(isNewMember)
                .onboardingCompleted(onboardingCompleted)
                .build();
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