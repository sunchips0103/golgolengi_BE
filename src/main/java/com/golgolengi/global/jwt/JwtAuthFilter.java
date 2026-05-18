package com.golgolengi.global.jwt;

import com.golgolengi.auth.repository.LogoutTokenRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final LogoutTokenRepository logoutTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                jwtProvider.validate(token);

                if (logoutTokenRepository.existsByToken(token)) {
                    throw new CustomException(ErrorCode.LOGOUT_TOKEN);
                }

                String memberId = jwtProvider.extractMemberId(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(memberId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (CustomException e) {
                request.setAttribute("exception", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}