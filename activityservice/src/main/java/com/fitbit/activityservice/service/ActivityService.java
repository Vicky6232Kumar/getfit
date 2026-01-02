package com.fitbit.activityservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fitbit.activityservice.dto.ActivityRequest;
import com.fitbit.activityservice.dto.ActivityResponse;
import com.fitbit.activityservice.model.Activity;
import com.fitbit.activityservice.repository.ActivityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository actRepo;
    private final UserValidationService userValdationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("{rabbitmq.routing.key}")
    private String routingKey;

    public ActivityResponse trackActivity(ActivityRequest req){

        boolean isValidUser = userValdationService.validateUser(req.getUserId());
        if(!isValidUser){
            throw new RuntimeException("Invalid User: " + req.getUserId());
        }
        Activity activity = Activity.builder().userId(req.getUserId()).addtionalMetrics(req.getAddtionalMetrics()).type(req.getType()).duration(req.getDuration()).startTime(req.getStartTime()).caloriesBurned(req.getCaloriesBurned()).build();

        Activity savedActivity = actRepo.save(activity);

        // Publish to RabbitMq for ai recommendation

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        } catch (Exception e) {
            log.error("Failed to publish activity in the mq", e);
        }

        return mapToResponse(savedActivity);

    }

    public List<ActivityResponse> getUserActivities(String userId){
        List<Activity> activities = actRepo.findByUserId(userId);

        return activities.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    

    public ActivityResponse getActivityDetails(String activityId) {
        return actRepo.findById(activityId).map(this::mapToResponse).orElseThrow(() -> new RuntimeException("Activity not found with activityId" + activityId));
    }

    public ActivityResponse mapToResponse(Activity savedActivity){
        ActivityResponse activityResponse = new ActivityResponse();
        activityResponse.setAddtionalMetrics(savedActivity.getAddtionalMetrics());
        activityResponse.setCaloriesBurned(savedActivity.getCaloriesBurned());
        activityResponse.setCreatedAt(savedActivity.getCreatedAt());
        activityResponse.setDuration(savedActivity.getDuration());
        activityResponse.setId(savedActivity.getId());
        activityResponse.setStartTime(savedActivity.getStartTime());
        activityResponse.setType(savedActivity.getType());
        activityResponse.setUpdatedAt(savedActivity.getUpdatedAt());
        activityResponse.setUserId(savedActivity.getUserId());

        return activityResponse;
    }

    
}
