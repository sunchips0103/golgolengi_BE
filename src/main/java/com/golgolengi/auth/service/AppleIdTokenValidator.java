package com.golgolengi.auth.service;

import com.golgolengi.auth.dto.AppleClaims;
import com.golgolengi.global.config.AppleAuthProperties;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.global.util.JwksClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

@Component
@RequiredArgsConstructor
public class AppleIdTokenValidator {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER   = "https://appleid.apple.com";

    private final JwksClient jwksClient;
    private final AppleAuthProperties appleAuthProperties;

    public AppleClaims validate(String idToken) {
        try {
            RSAPublicKey publicKey = jwksClient.fetchPublicKeys(APPLE_JWKS_URL)
                    .get(jwksClient.extractKid(idToken));
            if (publicKey == null) throw new CustomException(ErrorCode.INVALID_APPLE_TOKEN);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            if (!claims.getAudience().contains(appleAuthProperties.getClientId())) {
                throw new CustomException(ErrorCode.INVALID_APPLE_TOKEN);
            }

            return new AppleClaims(claims.getSubject(), claims.get("email", String.class));
        } catch (CustomException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_APPLE_TOKEN);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_APPLE_TOKEN);
        }
    }
}