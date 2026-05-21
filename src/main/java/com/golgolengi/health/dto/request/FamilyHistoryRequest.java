package com.golgolengi.health.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class FamilyHistoryRequest {
    private String relation;           // FATHER, MOTHER, SIBLING, GRANDPARENT
    private List<String> conditions;   // HYPERTENSION, DIABETES, HEART_DISEASE, CANCER, STROKE
}
