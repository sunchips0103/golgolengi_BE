package com.golgolengi.family.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateFamilyRequest {
    @NotBlank
    private String name;
}