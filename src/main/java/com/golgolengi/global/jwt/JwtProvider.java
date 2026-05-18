package com.golgolengi.global.jwt;

import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(String memberId) {
        return buildToken(memberId, jwtProperties.getAccessExpiration());
    }

    public String generateRefreshToken(String memberId) {
        return buildToken(memberId, jwtProperties.getRefreshExpiration());
    }

    public String extractMemberId(String token) {
        return parseClaims(token).getSubject();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public void validate(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String buildToken(String memberId, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(signingKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}