package com.fitbit.aiservice.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class Activity {
    private String id;
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> addtionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
