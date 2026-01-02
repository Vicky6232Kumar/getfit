package com.fitbit.activityservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fitbit.activityservice.model.ActivityType;

import lombok.Data;

@Data
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> addtionalMetrics;
}
