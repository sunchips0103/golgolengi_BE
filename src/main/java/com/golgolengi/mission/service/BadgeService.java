package com.golgolengi.mission.service;

import com.golgolengi.mission.domain.Badge;
import com.golgolengi.mission.dto.response.BadgeResponse;
import com.golgolengi.mission.repository.BadgeRepository;
import com.golgolengi.mission.repository.MissionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final MissionLogRepository missionLogRepository;

    public List<BadgeResponse> getBadges(String memberId) {
        return badgeRepository.findByMemberId(memberId)
                .stream()
                .map(BadgeResponse::from)
                .toList();
    }

    public void checkAndAward(String memberId) {
        awardIfAbsent(memberId, "FIRST_CHECKIN", () ->
                missionLogRepository.countByMemberId(memberId) >= 1);

        awardIfAbsent(memberId, "STREAK_3", () ->
                calculateStreak(memberId) >= 3);

        awardIfAbsent(memberId, "STREAK_7", () ->
                calculateStreak(memberId) >= 7);
    }

    private void awardIfAbsent(String memberId, String badgeType, java.util.function.BooleanSupplier condition) {
        if (!badgeRepository.existsByMemberIdAndBadgeType(memberId, badgeType) && condition.getAsBoolean()) {
            badgeRepository.save(Badge.builder()
                    .memberId(memberId)
                    .badgeType(badgeType)
                    .awardedAt(LocalDateTime.now())
                    .build());
        }
    }

    private int calculateStreak(String memberId) {
        List<LocalDate> checkInDates = missionLogRepository
                .findByMemberIdOrderByCheckedAtDesc(memberId)
                .stream()
                .map(log -> log.getCheckedAt().toLocalDate())
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .toList();

        if (checkInDates.isEmpty()) return 0;

        int streak = 1;
        for (int i = 0; i < checkInDates.size() - 1; i++) {
            if (checkInDates.get(i).minusDays(1).equals(checkInDates.get(i + 1))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
