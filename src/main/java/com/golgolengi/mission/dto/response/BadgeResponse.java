package com.golgolengi.mission.dto.response;

import com.golgolengi.mission.domain.Badge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeResponse {
    private String badgeType;
    private LocalDateTime awardedAt;

    public static BadgeResponse from(Badge badge) {
        return BadgeResponse.builder()
                .badgeType(badge.getBadgeType())
                .awardedAt(badge.getAwardedAt())
                .build();
    }
}
