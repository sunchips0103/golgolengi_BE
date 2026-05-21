package com.golgolengi.health.dto.request;

import lombok.Getter;

@Getter
public class UpdateLifestyleRequest {
    private boolean smoker;
    private String drinkingFrequency;
    private String exerciseFrequency;
    private String dietQuality;
    private int sleepHours;
}
