package com.golgolengi.risk.service;

import com.golgolengi.family.repository.FamilyMemberRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.member.domain.Member;
import com.golgolengi.member.repository.MemberRepository;
import com.golgolengi.risk.domain.RiskScore;
import com.golgolengi.risk.dto.response.*;
import com.golgolengi.risk.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RiskQueryService {

    private final RiskScoreRepository riskScoreRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberRepository memberRepository;

    public RiskScoreResponse getLatest(String memberId) {
        RiskScore rs = riskScoreRepository.findTopByMemberIdOrderByCalculatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.RISK_SCORE_NOT_FOUND));
        return RiskScoreResponse.from(rs);
    }

    public List<RiskScoreResponse> getHistory(String memberId) {
        return riskScoreRepository.findByMemberIdOrderByCalculatedAtDesc(memberId)
                .stream().map(RiskScoreResponse::from).toList();
    }

    public RiskBreakdownResponse getBreakdown(String memberId) {
        RiskScore rs = riskScoreRepository.findTopByMemberIdOrderByCalculatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.RISK_SCORE_NOT_FOUND));
        return RiskBreakdownResponse.from(rs);
    }

    public MemberRiskSummaryResponse getMemberSummary(String memberId) {
        RiskScore rs = riskScoreRepository.findTopByMemberIdOrderByCalculatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.RISK_SCORE_NOT_FOUND));
        return MemberRiskSummaryResponse.from(memberId, rs);
    }

    public FamilyRiskSummaryResponse getFamilyRiskSummary(String familyId) {
        List<String> memberIds = familyMemberRepository.findByFamilyId(familyId)
                .stream().map(fm -> fm.getMemberId()).toList();

        List<MemberRiskSummaryResponse> memberSummaries = memberIds.stream()
                .flatMap(id -> riskScoreRepository.findTopByMemberIdOrderByCalculatedAtDesc(id)
                        .map(rs -> MemberRiskSummaryResponse.from(id, rs))
                        .stream())
                .toList();

        double avg = memberSummaries.stream()
                .mapToDouble(MemberRiskSummaryResponse::getTotalScore)
                .average()
                .orElse(0.0);

        return FamilyRiskSummaryResponse.builder()
                .familyId(familyId)
                .averageScore(Math.round(avg * 100.0) / 100.0)
                .members(memberSummaries)
                .build();
    }

    public List<RankingResponse> getFamilyRanking(String familyId) {
        List<String> memberIds = familyMemberRepository.findByFamilyId(familyId)
                .stream().map(fm -> fm.getMemberId()).toList();

        AtomicInteger rankCounter = new AtomicInteger(1);

        return memberIds.stream()
                .flatMap(id -> riskScoreRepository.findTopByMemberIdOrderByCalculatedAtDesc(id)
                        .map(rs -> {
                            Member member = memberRepository.findById(new ObjectId(id)).orElse(null);
                            return RankingResponse.builder()
                                    .memberId(id)
                                    .name(member != null ? member.getName() : "")
                                    .profileImageUrl(member != null ? member.getProfileImageUrl() : null)
                                    .totalScore(rs.getTotalScore())
                                    .riskLevel(MemberRiskSummaryResponse.calcRiskLevel(rs.getTotalScore()))
                                    .build();
                        })
                        .stream())
                .sorted(java.util.Comparator.comparingDouble(RankingResponse::getTotalScore))
                .map(r -> RankingResponse.builder()
                        .rank(rankCounter.getAndIncrement())
                        .memberId(r.getMemberId())
                        .name(r.getName())
                        .profileImageUrl(r.getProfileImageUrl())
                        .totalScore(r.getTotalScore())
                        .riskLevel(r.getRiskLevel())
                        .build())
                .toList();
    }
}
