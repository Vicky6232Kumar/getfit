package com.fitbit.aiservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fitbit.aiservice.model.Recommendation;
import com.fitbit.aiservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepo;

    public List<Recommendation> getUserRecommendation(String userId){
        return recommendationRepo.findByUserId(userId);
    }

    public Recommendation getActivityRecommendation(String activityId){
        return recommendationRepo.findByActivityId(activityId).orElseThrow(() -> new RuntimeException("No recommendation found for this activity" + activityId));
    }

}
