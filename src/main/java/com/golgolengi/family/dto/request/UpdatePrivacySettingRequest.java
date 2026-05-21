package com.golgolengi.family.dto.request;

import lombok.Getter;

@Getter
public class UpdatePrivacySettingRequest {
    private boolean riskScoreVisible;
    private boolean missionVisible;
}