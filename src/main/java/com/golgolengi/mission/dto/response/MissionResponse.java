package com.golgolengi.mission.dto.response;

import com.golgolengi.mission.domain.Mission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MissionResponse {
    private String missionId;
    private String familyId;
    private String title;
    private String description;
    private String category;
    private String status;
    private int targetCount;
    private String unit;
    private LocalDate endDate;

    public static MissionResponse from(Mission mission) {
        return MissionResponse.builder()
                .missionId(mission.getId().toHexString())
                .familyId(mission.getFamilyId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .category(mission.getCategory())
                .status(mission.getStatus())
                .targetCount(mission.getTargetCount())
                .unit(mission.getUnit())
                .endDate(mission.getEndDate())
                .build();
    }
}
