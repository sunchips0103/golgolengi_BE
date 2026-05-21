package com.golgolengi.health.service;

import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.health.domain.HealthProfile;
import com.golgolengi.health.dto.request.*;
import com.golgolengi.health.dto.response.HealthProfileResponse;
import com.golgolengi.health.repository.HealthProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthProfileRepository healthProfileRepository;

    public HealthProfileResponse createProfile(String memberId, CreateHealthProfileRequest request) {
        if (healthProfileRepository.existsByMemberId(memberId)) {
            throw new CustomException(ErrorCode.HEALTH_PROFILE_ALREADY_EXISTS);
        }
        HealthProfile profile = healthProfileRepository.save(HealthProfile.builder()
                .memberId(memberId)
                .conditions(request.getConditions() != null ? request.getConditions() : List.of())
                .build());
        return HealthProfileResponse.from(profile);
    }

    public HealthProfileResponse getProfile(String profileId, String memberId) {
        HealthProfile profile = findByMemberId(memberId);
        return HealthProfileResponse.from(profile);
    }

    public HealthProfileResponse addConditions(String profileId, CreateHealthProfileRequest request) {
        HealthProfile profile = findById(profileId);
        profile.getConditions().addAll(request.getConditions());
        healthProfileRepository.save(profile);
        return HealthProfileResponse.from(profile);
    }

    public HealthProfileResponse addFamilyHistory(String profileId, FamilyHistoryRequest request) {
        HealthProfile profile = findById(profileId);
        request.getItems().forEach(item ->
                profile.getFamilyHistory().add(new HealthProfile.FamilyHistoryItem(item.getRelation(), item.getCondition()))
        );
        healthProfileRepository.save(profile);
        return HealthProfileResponse.from(profile);
    }

    public HealthProfileResponse updateLifestyle(String profileId, UpdateLifestyleRequest request) {
        HealthProfile profile = findById(profileId);
        profile.setLifestyle(HealthProfile.Lifestyle.builder()
                .smoker(request.isSmoker())
                .drinkingFrequency(request.getDrinkingFrequency())
                .exerciseFrequency(request.getExerciseFrequency())
                .dietQuality(request.getDietQuality())
                .sleepHours(request.getSleepHours())
                .build());
        healthProfileRepository.save(profile);
        return HealthProfileResponse.from(profile);
    }

    public void syncHealthData(String memberId, SyncHealthDataRequest request) {
        HealthProfile profile = findByMemberId(memberId);
        request.getData().forEach(d ->
                profile.getHealthData().add(HealthProfile.HealthDataPoint.builder()
                        .type(d.getType())
                        .value(d.getValue())
                        .unit(d.getUnit())
                        .recordedAt(d.getRecordedAt())
                        .build())
        );
        // 30일 이전 데이터 자동 제거
        profile.getHealthData().removeIf(d -> d.getRecordedAt().isBefore(LocalDateTime.now().minusDays(30)));
        healthProfileRepository.save(profile);
    }

    public HealthProfile findByMemberId(String memberId) {
        return healthProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.HEALTH_PROFILE_NOT_FOUND));
    }

    private HealthProfile findById(String profileId) {
        return healthProfileRepository.findById(new org.bson.types.ObjectId(profileId))
                .orElseThrow(() -> new CustomException(ErrorCode.HEALTH_PROFILE_NOT_FOUND));
    }
}
