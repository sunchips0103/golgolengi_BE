package com.golgolengi.member.service;

import com.golgolengi.family.repository.FamilyMemberRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.member.domain.Member;
import com.golgolengi.member.dto.request.UpdateMemberRequest;
import com.golgolengi.member.dto.response.MemberResponse;
import com.golgolengi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public MemberResponse getMe(String memberId) {
        Member member = findMember(memberId);
        String familyId = familyMemberRepository.findByMemberId(memberId)
                .map(fm -> fm.getFamilyId())
                .orElse(null);
        return MemberResponse.of(member, familyId);
    }

    public void completeOnboarding(String memberId) {
        Member member = findMember(memberId);
        member.setOnboardingCompleted(true);
        memberRepository.save(member);
    }

    public MemberResponse updateMember(String memberId, UpdateMemberRequest request) {
        Member member = findMember(memberId);
        if (request.getName() != null) member.setName(request.getName());
        if (request.getProfileImageUrl() != null) member.setProfileImageUrl(request.getProfileImageUrl());
        memberRepository.save(member);
        String familyId = familyMemberRepository.findByMemberId(memberId)
                .map(fm -> fm.getFamilyId())
                .orElse(null);
        return MemberResponse.of(member, familyId);
    }

    public void withdraw(String memberId) {
        Member member = findMember(memberId);
        member.setDeletedAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    public Member findMember(String memberId) {
        return memberRepository.findById(new ObjectId(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
