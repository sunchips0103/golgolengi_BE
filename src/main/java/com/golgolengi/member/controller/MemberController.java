package com.golgolengi.member.controller;

import com.golgolengi.global.response.ApiResponse;
import com.golgolengi.member.dto.request.UpdateMemberRequest;
import com.golgolengi.member.dto.response.MemberResponse;
import com.golgolengi.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMe(
            @AuthenticationPrincipal String memberId) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMe(memberId)));
    }

    @PatchMapping("/member/onboarding")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @AuthenticationPrincipal String memberId) {
        memberService.completeOnboarding(memberId);
        return ResponseEntity.ok(ApiResponse.ok("온보딩이 완료되었습니다."));
    }

    @PatchMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable String memberId,
            @RequestBody UpdateMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.updateMember(memberId, request)));
    }
}