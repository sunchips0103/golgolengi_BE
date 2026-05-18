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
@Document(collection = "consents")
public class Consent {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String memberId;

    private boolean termsAgreed;
    private boolean privacyAgreed;
    private boolean marketingAgreed;

    @CreatedDate
    private LocalDateTime agreedAt;
}