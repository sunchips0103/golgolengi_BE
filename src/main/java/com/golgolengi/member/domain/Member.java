package com.golgolengi.member.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "members")
@CompoundIndex(name = "social_unique", def = "{'socialProvider': 1, 'socialId': 1}", unique = true)
public class Member {

    @Id
    private ObjectId id;

    private String socialProvider;
    private String socialId;
    private String email;
    private String name;
    private String profileImageUrl;
    private String fcmToken;

    @Builder.Default
    private boolean onboardingCompleted = false;

    private LocalDateTime deletedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}