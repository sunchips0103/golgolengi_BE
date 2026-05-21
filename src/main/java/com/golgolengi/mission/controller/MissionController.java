package com.golgolengi.mission.controller;

import com.golgolengi.global.response.ApiResponse;
import com.golgolengi.mission.dto.request.CheckInRequest;
import com.golgolengi.mission.dto.response.MissionResponse;
import com.golgolengi.mission.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping("/missions/recommended")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getRecommended(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.getRecommended(memberId)));
    }

    @GetMapping("/missions")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getMissions(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.getMissions(memberId)));
    }

    @PostMapping("/mission-logs")
    public ResponseEntity<ApiResponse<MissionResponse>> checkIn(
            @AuthenticationPrincipal String memberId,
            @Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(missionService.checkIn(memberId, request)));
    }
}
