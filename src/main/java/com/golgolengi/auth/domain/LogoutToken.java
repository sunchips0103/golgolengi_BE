package com.golgolengi.auth.domain;

import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "logout_tokens")
public class LogoutToken {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String token;

    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;
}