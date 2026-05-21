package com.golgolengi.risk.controller;

import com.golgolengi.family.repository.FamilyMemberRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.global.response.ApiResponse;
import com.golgolengi.risk.dto.response.*;
import com.golgolengi.risk.service.RiskCalculatorService;
import com.golgolengi.risk.service.RiskQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RiskController {

    private final RiskCalculatorService riskCalculatorService;
    private final RiskQueryService riskQueryService;
    private final FamilyMemberRepository familyMemberRepository;

    @PostMapping("/risk-scores/calculate")
    public ResponseEntity<ApiResponse<RiskBreakdownResponse>> calculate(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(riskCalculatorService.calculate(memberId)));
    }

    @GetMapping("/risk-scores")
    public ResponseEntity<ApiResponse<RiskScoreResponse>> getLatest(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(riskQueryService.getLatest(memberId)));
    }

    @GetMapping("/risk-scores/history")
    public ResponseEntity<ApiResponse<List<RiskScoreResponse>>> getHistory(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(riskQueryService.getHistory(memberId)));
    }

    @GetMapping("/risk-scores/breakdown")
    public ResponseEntity<ApiResponse<RiskBreakdownResponse>> getBreakdown(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(riskQueryService.getBreakdown(memberId)));
    }

    @GetMapping("/members/{memberId}/risk-summary")
    public ResponseEntity<ApiResponse<MemberRiskSummaryResponse>> getMemberRiskSummary(
            @PathVariable String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(riskQueryService.getMemberSummary(memberId)));
    }

    @GetMapping("/families/{familyId}/risk-summary")
    public ResponseEntity<ApiResponse<FamilyRiskSummaryResponse>> getFamilyRiskSummary(
            @AuthenticationPrincipal String memberId,
            @PathVariable String familyId) {
        // 요청자가 해당 가족 구성원인지 검증
        familyMemberRepository.findByFamilyIdAndMemberId(familyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FAMILY_MEMBER));
        return ResponseEntity.ok(ApiResponse.ok(riskQueryService.getFamilyRiskSummary(familyId)));
    }

    @GetMapping("/families/{familyId}/today-status")
    public ResponseEntity<ApiResponse<FamilyRiskSummaryResponse>> getFamilyTodayStatus(
            @AuthenticationPrincipal String memberId,
            @PathVariable String familyId) {
        familyMemberRepository.findByFamilyIdAndMemberId(familyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FAMILY_MEMBER));
        return ResponseEntity.ok(ApiResponse.ok(riskQueryService.getFamilyRiskSummary(familyId)));
    }
}
