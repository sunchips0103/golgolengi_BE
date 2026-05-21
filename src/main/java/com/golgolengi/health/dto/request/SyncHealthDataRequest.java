package com.golgolengi.health.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SyncHealthDataRequest {
    private List<HealthDataPointRequest> data;

    @Getter
    public static class HealthDataPointRequest {
        private String type;
        private double value;
        private String unit;
        private LocalDateTime recordedAt;
    }
}
