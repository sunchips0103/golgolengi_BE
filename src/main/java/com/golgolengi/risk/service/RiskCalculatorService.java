package com.golgolengi.risk.service;

import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.health.domain.HealthProfile;
import com.golgolengi.health.domain.HealthProfile.FamilyHistoryItem;
import com.golgolengi.health.domain.HealthProfile.HealthDataPoint;
import com.golgolengi.health.domain.HealthProfile.Lifestyle;
import com.golgolengi.health.repository.HealthProfileRepository;
import com.golgolengi.risk.domain.RiskScore;
import com.golgolengi.risk.dto.response.RiskBreakdownResponse;
import com.golgolengi.risk.dto.response.RiskScoreResponse;
import com.golgolengi.risk.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class RiskCalculatorService {

    private static final double W_GENETIC    = 0.30;
    private static final double W_LIFESTYLE  = 0.25;
    private static final double W_BEHAVIOR   = 0.20;
    private static final double W_ENVIRONMENT = 0.15;
    private static final double W_CLINICAL   = 0.10;

    private final HealthProfileRepository healthProfileRepository;
    private final RiskScoreRepository riskScoreRepository;

    public RiskBreakdownResponse calculate(String memberId) {
        HealthProfile profile = healthProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.HEALTH_PROFILE_NOT_FOUND));

        double genetic     = calcGenetic(profile.getFamilyHistory());
        double lifestyle   = calcLifestyle(profile.getLifestyle());
        double behavior    = calcBehavior(profile.getHealthData());
        double environment = 50.0; // MVP: 환경 데이터 없음, 기본값
        double clinical    = calcClinical(profile.getConditions());

        double total = genetic * W_GENETIC
                + lifestyle * W_LIFESTYLE
                + behavior * W_BEHAVIOR
                + environment * W_ENVIRONMENT
                + clinical * W_CLINICAL;

        RiskScore saved = riskScoreRepository.save(RiskScore.builder()
                .memberId(memberId)
                .totalScore(round(total))
                .geneticScore(round(genetic))
                .lifestyleScore(round(lifestyle))
                .behaviorScore(round(behavior))
                .environmentScore(round(environment))
                .clinicalScore(round(clinical))
                .build());

        return RiskBreakdownResponse.from(saved);
    }

    // ── 세부 점수 계산 ────────────────────────────────────────────────────────

    private double calcGenetic(List<FamilyHistoryItem> history) {
        if (history == null || history.isEmpty()) return 0;
        double score = 0;
        for (FamilyHistoryItem item : history) {
            String c = item.getCondition();
            if (c == null) continue;
            score += switch (c) {
                case "HEART_DISEASE" -> 30;
                case "CANCER"        -> 30;
                case "STROKE"        -> 25;
                case "HYPERTENSION"  -> 20;
                case "DIABETES"      -> 20;
                default              -> 5;
            };
        }
        return Math.min(score, 100);
    }

    private double calcLifestyle(Lifestyle ls) {
        if (ls == null) return 50;
        double score = 0;
        if (ls.isSmoker()) score += 30;
        score += switch (ls.getDrinkingFrequency() == null ? "NONE" : ls.getDrinkingFrequency()) {
            case "FREQUENT"   -> 25;
            case "OCCASIONAL" -> 10;
            default           -> 0;
        };
        score += switch (ls.getExerciseFrequency() == null ? "NONE" : ls.getExerciseFrequency()) {
            case "NONE"           -> 30;
            case "ONCE_WEEK"      -> 20;
            case "TWO_THREE_WEEK" -> 10;
            default               -> 0;  // DAILY
        };
        score += switch (ls.getDietQuality() == null ? "FAIR" : ls.getDietQuality()) {
            case "POOR" -> 30;
            case "FAIR" -> 15;
            default     -> 0;  // GOOD
        };
        int sleep = ls.getSleepHours();
        if (sleep < 6) score += 20;
        else if (sleep < 7) score += 10;
        else if (sleep > 9) score += 5;
        return Math.min(score, 100);
    }

    private double calcBehavior(List<HealthDataPoint> data) {
        if (data == null || data.isEmpty()) return 50;
        OptionalDouble avgSteps = data.stream()
                .filter(d -> "STEPS".equals(d.getType()))
                .mapToDouble(HealthDataPoint::getValue)
                .average();
        if (avgSteps.isEmpty()) return 50;
        double steps = avgSteps.getAsDouble();
        if (steps >= 8000) return 10;
        if (steps >= 5000) return 30;
        if (steps >= 3000) return 60;
        return 80;
    }

    private double calcClinical(List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) return 0;
        double score = 0;
        for (String c : conditions) {
            score += switch (c) {
                case "HEART_DISEASE" -> 40;
                case "CANCER"        -> 40;
                case "STROKE"        -> 35;
                case "HYPERTENSION"  -> 30;
                case "DIABETES"      -> 30;
                default              -> 5;
            };
        }
        return Math.min(score, 100);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
