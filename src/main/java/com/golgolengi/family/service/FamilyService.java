package com.golgolengi.family.service;

import com.golgolengi.family.domain.Family;
import com.golgolengi.family.domain.FamilyMember;
import com.golgolengi.family.dto.request.CreateFamilyRequest;
import com.golgolengi.family.dto.request.JoinFamilyRequest;
import com.golgolengi.family.dto.request.UpdatePrivacySettingRequest;
import com.golgolengi.family.dto.response.*;
import com.golgolengi.family.repository.FamilyMemberRepository;
import com.golgolengi.family.repository.FamilyRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.member.domain.Member;
import com.golgolengi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberRepository memberRepository;

    public FamilyResponse createFamily(String memberId, CreateFamilyRequest request) {
        if (familyMemberRepository.existsByMemberId(memberId)) {
            throw new CustomException(ErrorCode.ALREADY_IN_FAMILY);
        }
        Family family = familyRepository.save(Family.builder()
                .name(request.getName())
                .inviteCode(generateInviteCode())
                .createdByMemberId(memberId)
                .build());

        familyMemberRepository.save(FamilyMember.builder()
                .familyId(family.getId().toHexString())
                .memberId(memberId)
                .role("OWNER")
                .build());

        return FamilyResponse.from(family);
    }

    public FamilyResponse joinFamily(String memberId, JoinFamilyRequest request) {
        if (familyMemberRepository.existsByMemberId(memberId)) {
            throw new CustomException(ErrorCode.ALREADY_IN_FAMILY);
        }
        Family family = familyRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        familyMemberRepository.save(FamilyMember.builder()
                .familyId(family.getId().toHexString())
                .memberId(memberId)
                .role("MEMBER")
                .build());

        return FamilyResponse.from(family);
    }

    public InviteCodeResponse refreshInviteCode(String familyId, String memberId) {
        Family family = findFamily(familyId);
        validateMember(familyId, memberId);
        family.setInviteCode(generateInviteCode());
        familyRepository.save(family);
        return new InviteCodeResponse(family.getInviteCode());
    }

    public InviteCodeResponse getInviteCode(String familyId, String memberId) {
        Family family = findFamily(familyId);
        validateMember(familyId, memberId);
        return new InviteCodeResponse(family.getInviteCode());
    }

    public FamilyOverviewResponse getOverview(String familyId, String memberId) {
        Family family = findFamily(familyId);
        validateMember(familyId, memberId);
        List<FamilyMemberResponse> members = buildMemberResponses(familyId);
        return FamilyOverviewResponse.builder()
                .familyId(familyId)
                .name(family.getName())
                .memberCount(members.size())
                .members(members)
                .build();
    }

    public List<FamilyMemberResponse> getMembers(String familyId, String memberId) {
        validateMember(familyId, memberId);
        return buildMemberResponses(familyId);
    }

    public void updatePrivacySetting(String memberId, UpdatePrivacySettingRequest request) {
        FamilyMember fm = familyMemberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        fm.getPrivacySetting().setRiskScoreVisible(request.isRiskScoreVisible());
        fm.getPrivacySetting().setMissionVisible(request.isMissionVisible());
        familyMemberRepository.save(fm);
    }

    public void removeMember(String familyId, String targetMemberId, String requestMemberId) {
        validateMember(familyId, requestMemberId);
        familyMemberRepository.deleteByFamilyIdAndMemberId(familyId, targetMemberId);
    }

    // ── 내부 유틸 ──────────────────────────────────────────────────────────────

    private Family findFamily(String familyId) {
        return familyRepository.findById(new ObjectId(familyId))
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_NOT_FOUND));
    }

    private void validateMember(String familyId, String memberId) {
        familyMemberRepository.findByFamilyIdAndMemberId(familyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FAMILY_MEMBER));
    }

    private List<FamilyMemberResponse> buildMemberResponses(String familyId) {
        return familyMemberRepository.findByFamilyId(familyId).stream()
                .map(fm -> {
                    Member member = memberRepository.findById(new ObjectId(fm.getMemberId()))
                            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
                    return FamilyMemberResponse.builder()
                            .memberId(fm.getMemberId())
                            .name(member.getName())
                            .profileImageUrl(member.getProfileImageUrl())
                            .role(fm.getRole())
                            .riskScoreVisible(fm.getPrivacySetting().isRiskScoreVisible())
                            .missionVisible(fm.getPrivacySetting().isMissionVisible())
                            .build();
                })
                .toList();
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
