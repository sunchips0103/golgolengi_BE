package com.golgolengi.health.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class CreateHealthProfileRequest {
    private List<String> conditions;
}
