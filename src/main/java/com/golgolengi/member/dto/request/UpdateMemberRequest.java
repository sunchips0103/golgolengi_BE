package com.golgolengi.member.dto.request;

import lombok.Getter;

@Getter
public class UpdateMemberRequest {
    private String name;
    private String profileImageUrl;
}