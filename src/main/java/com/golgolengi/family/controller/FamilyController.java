package com.golgolengi.family.controller;

import com.golgolengi.family.dto.request.CreateFamilyRequest;
import com.golgolengi.family.dto.request.JoinFamilyRequest;
import com.golgolengi.family.dto.request.UpdatePrivacySettingRequest;
import com.golgolengi.family.dto.response.*;
import com.golgolengi.family.service.FamilyService;
import com.golgolengi.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping("/families")
    public ResponseEntity<ApiResponse<FamilyResponse>> createFamily(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid CreateFamilyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(familyService.createFamily(memberId, request)));
    }

    @PostMapping("/families/join")
    public ResponseEntity<ApiResponse<FamilyResponse>> joinFamily(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid JoinFamilyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(familyService.joinFamily(memberId, request)));
    }

    @PostMapping("/families/{familyId}/invite")
    public ResponseEntity<ApiResponse<InviteCodeResponse>> refreshInviteCode(
            @PathVariable String familyId,
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(familyService.refreshInviteCode(familyId, memberId)));
    }

    @GetMapping("/families/{familyId}/invite-code")
    public ResponseEntity<ApiResponse<InviteCodeResponse>> getInviteCode(
            @PathVariable String familyId,
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(familyService.getInviteCode(familyId, memberId)));
    }

    @GetMapping("/families/{familyId}/overview")
    public ResponseEntity<ApiResponse<FamilyOverviewResponse>> getOverview(
            @PathVariable String familyId,
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(familyService.getOverview(familyId, memberId)));
    }

    @GetMapping("/families/{familyId}/members")
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> getMembers(
            @PathVariable String familyId,
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(familyService.getMembers(familyId, memberId)));
    }

    @PatchMapping("/privacy-settings/{targetMemberId}")
    public ResponseEntity<ApiResponse<Void>> updatePrivacySetting(
            @PathVariable String targetMemberId,
            @RequestBody UpdatePrivacySettingRequest request) {
        familyService.updatePrivacySetting(targetMemberId, request);
        return ResponseEntity.ok(ApiResponse.ok("공개 설정이 변경되었습니다."));
    }

    @DeleteMapping("/family-members/{targetMemberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String targetMemberId,
            @AuthenticationPrincipal String memberId,
            @RequestParam String familyId) {
        familyService.removeMember(familyId, targetMemberId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("가족 구성원이 제거되었습니다."));
    }
}
