package com.golgolengi.mission.domain;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "missions")
@CompoundIndex(name = "family_status_idx", def = "{'familyId': 1, 'status': 1}")
public class Mission {

    @Id
    private ObjectId id;

    private String familyId;
    private String title;
    private String description;
    private String category;   // WALK, WATER, SLEEP, DIET, MEDITATION
    private String status;     // ACTIVE, COMPLETED, POSTPONED

    private int targetCount;
    private String unit;

    @Indexed
    private LocalDate endDate;

    @CreatedDate
    private LocalDateTime createdAt;
}
