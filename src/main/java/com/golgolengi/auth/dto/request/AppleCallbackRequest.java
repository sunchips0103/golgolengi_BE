package com.golgolengi.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleCallbackRequest(
        @JsonProperty("id_token") String idToken,
        String code,
        UserInfo user
) {
    public record UserInfo(String name, String email) {}
}
