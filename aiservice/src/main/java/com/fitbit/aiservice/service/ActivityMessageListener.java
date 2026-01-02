package com.fitbit.aiservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fitbit.aiservice.model.Activity;
import com.fitbit.aiservice.model.Recommendation;
import com.fitbit.aiservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    
    private final ActivityAiService activityAiService;
    private final RecommendationRepository recommendationRepo;

    @RabbitListener(queues = "activity.queue")

    
    public void processActivity(Activity activity){

        // log.info("Recieved the message : {}", activity.getId());
        // log.info("GENERATED RECOMMENDATION", activityAiService.generateRecommendation(activity));
        Recommendation recommendation = activityAiService.generateRecommendation(activity);
        recommendationRepo.save(recommendation);

    }
}
