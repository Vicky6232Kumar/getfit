package com.fitbit.activityservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fitbit.activityservice.model.ActivityType;

import lombok.Data;

@Data
public class ActivityResponse {
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
