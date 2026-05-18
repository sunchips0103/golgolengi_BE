package com.golgolengi.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golgolengi.auth.service.OAuth2LoginSuccessHandler;
import com.golgolengi.global.jwt.JwtAuthFilter;
import com.golgolengi.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(e -> e.baseUri("/oauth/google/login"))
                        .redirectionEndpoint(e -> e.baseUri("/oauth/google/callback"))
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth/**", "/auth/refresh", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            res.getWriter().write(
                                    objectMapper.writeValueAsString(ApiResponse.fail("인증이 필요합니다."))
                            );
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            res.getWriter().write(
                                    objectMapper.writeValueAsString(ApiResponse.fail("접근 권한이 없습니다."))
                            );
                        })
                )
                .build();
    }
}