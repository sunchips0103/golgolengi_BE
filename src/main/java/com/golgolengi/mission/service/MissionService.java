package com.golgolengi.mission.service;

import com.golgolengi.family.repository.FamilyMemberRepository;
import com.golgolengi.global.exception.CustomException;
import com.golgolengi.global.exception.ErrorCode;
import com.golgolengi.health.domain.HealthProfile;
import com.golgolengi.health.service.HealthService;
import com.golgolengi.mission.domain.Mission;
import com.golgolengi.mission.domain.MissionLog;
import com.golgolengi.mission.dto.request.CheckInRequest;
import com.golgolengi.mission.dto.response.MissionResponse;
import com.golgolengi.mission.repository.MissionLogRepository;
import com.golgolengi.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionLogRepository missionLogRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final BadgeService badgeService;
    private final HealthService healthService;

    public List<MissionResponse> getRecommended(String memberId) {
        String familyId = familyMemberRepository.findByMemberId(memberId)
                .map(fm -> fm.getFamilyId())
                .orElse(null);

        // 이미 생성된 ACTIVE 미션이 있으면 반환
        if (familyId != null) {
            List<Mission> active = missionRepository.findByFamilyIdAndStatus(familyId, "ACTIVE");
            if (!active.isEmpty()) {
                return active.stream().map(MissionResponse::from).toList();
            }
        }

        // 건강 프로필 기반 추천 미션 생성
        return generateRecommendedMissions(memberId, familyId);
    }

    public List<MissionResponse> getMissions(String memberId) {
        String familyId = familyMemberRepository.findByMemberId(memberId)
                .map(fm -> fm.getFamilyId())
                .orElseThrow(() -> new CustomException(ErrorCode.FAMILY_MEMBER_NOT_FOUND));
        return missionRepository.findByFamilyIdAndStatus(familyId, "ACTIVE")
                .stream().map(MissionResponse::from).toList();
    }

    public MissionResponse checkIn(String memberId, CheckInRequest request) {
        Mission mission = missionRepository.findById(new ObjectId(request.getMissionId()))
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        missionLogRepository.save(MissionLog.builder()
                .missionId(request.getMissionId())
                .memberId(memberId)
                .value(request.getValue())
                .build());

        // 배지 자동 부여 (내부 호출 전용)
        badgeService.checkAndAward(memberId);

        return MissionResponse.from(mission);
    }

    // ── 추천 미션 생성 ──────────────────────────────────────────────────────────

    private List<MissionResponse> generateRecommendedMissions(String memberId, String familyId) {
        List<Mission> templates = buildMissionTemplates(memberId, familyId);
        List<Mission> saved = missionRepository.saveAll(templates);
        return saved.stream().map(MissionResponse::from).toList();
    }

    private List<Mission> buildMissionTemplates(String memberId, String familyId) {
        try {
            HealthProfile profile = healthService.findByMemberId(memberId);
            List<String> conditions = profile.getConditions();
            boolean hasCardiovascular = conditions.stream()
                    .anyMatch(c -> c.contains("HYPERTENSION") || c.contains("HEART_DISEASE"));
            boolean hasDiabetes = conditions.contains("DIABETES");

            if (hasCardiovascular) {
                return List.of(
                        walkMission(familyId),
                        meditationMission(familyId),
                        waterMission(familyId)
                );
            } else if (hasDiabetes) {
                return List.of(walkMission(familyId), dietMission(familyId), waterMission(familyId));
            }
        } catch (Exception ignored) {}

        return List.of(walkMission(familyId), waterMission(familyId), sleepMission(familyId));
    }

    private Mission walkMission(String familyId) {
        return Mission.builder().familyId(familyId).title("매일 걷기")
                .description("하루 8,000보 걷기").category("WALK")
                .status("ACTIVE").targetCount(8000).unit("steps")
                .endDate(LocalDate.now().plusDays(7)).build();
    }

    private Mission waterMission(String familyId) {
        return Mission.builder().familyId(familyId).title("물 마시기")
                .description("하루 8잔 마시기").category("WATER")
                .status("ACTIVE").targetCount(8).unit("glasses")
                .endDate(LocalDate.now().plusDays(7)).build();
    }

    private Mission sleepMission(String familyId) {
        return Mission.builder().familyId(familyId).title("숙면 챌린지")
                .description("7시간 이상 수면").category("SLEEP")
                .status("ACTIVE").targetCount(7).unit("hours")
                .endDate(LocalDate.now().plusDays(7)).build();
    }

    private Mission dietMission(String familyId) {
        return Mission.builder().familyId(familyId).title("건강 식단")
                .description("채소·과일 하루 5회 섭취").category("DIET")
                .status("ACTIVE").targetCount(5).unit("servings")
                .endDate(LocalDate.now().plusDays(7)).build();
    }

    private Mission meditationMission(String familyId) {
        return Mission.builder().familyId(familyId).title("명상하기")
                .description("하루 10분 명상").category("MEDITATION")
                .status("ACTIVE").targetCount(10).unit("minutes")
                .endDate(LocalDate.now().plusDays(7)).build();
    }
}
