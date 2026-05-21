package com.golgolengi.risk.service;

import com.golgolengi.family.repository.FamilyMemberRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.risk.domain.RiskScore;
import com.golgolengi.risk.dto.response.*;
import com.golgolengi.risk.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskQueryService {

    private final RiskScoreRepository riskScoreRepository;
    private final FamilyMemberRepository familyMemberRepository;

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
}
