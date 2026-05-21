package com.golgolengi.health.controller;

import com.golgolengi.global.response.ApiResponse;
import com.golgolengi.health.dto.request.*;
import com.golgolengi.health.dto.response.HealthProfileResponse;
import com.golgolengi.health.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @PostMapping("/health-profiles")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> createProfile(
            @AuthenticationPrincipal String memberId,
            @RequestBody CreateHealthProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(healthService.createProfile(memberId, request)));
    }

    @GetMapping("/health-profiles/{profileId}")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> getProfile(
            @PathVariable String profileId,
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(healthService.getProfile(profileId, memberId)));
    }

    @PostMapping("/health-profiles/{profileId}/conditions")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> addConditions(
            @PathVariable String profileId,
            @RequestBody CreateHealthProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(healthService.addConditions(profileId, request)));
    }

    @PostMapping("/health-profiles/{profileId}/family-history")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> addFamilyHistory(
            @PathVariable String profileId,
            @RequestBody FamilyHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(healthService.addFamilyHistory(profileId, request)));
    }

    @PatchMapping("/health-profiles/{profileId}/lifestyle")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> updateLifestyle(
            @PathVariable String profileId,
            @RequestBody UpdateLifestyleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(healthService.updateLifestyle(profileId, request)));
    }

    @PostMapping("/health-data/sync")
    public ResponseEntity<ApiResponse<Void>> syncHealthData(
            @AuthenticationPrincipal String memberId,
            @RequestBody SyncHealthDataRequest request) {
        healthService.syncHealthData(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("건강 데이터가 동기화되었습니다."));
    }
}
