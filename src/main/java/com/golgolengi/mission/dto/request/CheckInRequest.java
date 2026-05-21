package com.golgolengi.mission.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CheckInRequest {
    @NotBlank
    private String missionId;
    private int value;
}
