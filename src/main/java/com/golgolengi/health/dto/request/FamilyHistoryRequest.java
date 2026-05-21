package com.golgolengi.health.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class FamilyHistoryRequest {
    private List<FamilyHistoryItemRequest> items;

    @Getter
    public static class FamilyHistoryItemRequest {
        private String relation;
        private String condition;
    }
}
