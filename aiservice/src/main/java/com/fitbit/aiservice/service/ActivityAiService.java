package com.fitbit.aiservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitbit.aiservice.model.Activity;
import com.fitbit.aiservice.model.Recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {
    private final GeminiService geminiService;

    // this method handle the activity help for generate recommendation using gemini
    // api
    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);

        String response = geminiService.getAnswer(prompt);
        // log.info("RESPONSE FROM AI: {}", response);

        // we have to refine the ai response 
        return processAiResponse(activity, response);

    }

    // refine the ai response to insert into db
    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidates").get(0).path("contents").path("parts").get(0).path("text");

            String jsonContent = textNode.asText().replaceAll("```json\\n", "").replaceAll("\\n```", "").trim();
            JsonNode analysis = mapper.readTree(jsonContent);

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysis, "overall", "Overall :");
            addAnalysisSection(fullAnalysis, analysis, "pace", "Pace :");
            addAnalysisSection(fullAnalysis, analysis, "heartRate", "Heart Rate :");
            addAnalysisSection(fullAnalysis, analysis, "caloriesBurned", "Calories Burned :");

            List<String> improvments = extractImprovement(analysis);

            List<String> suggestions = extractSuggestions(analysis);

            List<String> safety = extractSafetyGuidelines(analysis);

            return Recommendation.builder().activityId(activity.getId()).userId(activity.getUserId()).activityType(activity.getType()).improvements(improvments).recommendation(fullAnalysis.toString().trim()).safety(safety).suggestions(suggestions).createdAt(LocalDateTime.now()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Recommendation.builder().activityId(activity.getId()).userId(activity.getUserId()).activityType(activity.getType()).improvements(Collections.singletonList("No specific improvements provided")).recommendation("Unable to generate detailed analysis").safety(Collections.singletonList("No specific safety is provided")).suggestions(Collections.singletonList("No specific suggestions is provided")).createdAt(LocalDateTime.now()).build();
        }
    }

    // refine the analysis
    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix).append(analysisNode.path(analysisNode.path(key).asText())).append("\\n");
        }
    }

    // refine the improvement list
    private List<String> extractImprovement(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();

        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String details = improvement.path("recommendation").asText();
                improvements.add(String.format("%s : %s", area, details));
            });
        }

        return improvements.isEmpty() ? Collections.singletonList("No specific improvements provided") : improvements;
    }

    // refine the suggestion list
    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if(suggestionsNode.isArray()){
            suggestionsNode.forEach(improvement -> {
                String workout = improvement.path("workout").asText();
                String description = improvement.path("description").asText();
                suggestions.add(String.format("%s : %s", workout, description));
            });
        }

        return suggestions.isEmpty() ? Collections.singletonList("No specific suggestions is provided") : suggestions;
    }

    // refine the safety guideline
    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safetyList = new ArrayList<>();
        if(safetyNode.isArray()){
            safetyNode.forEach(safety -> {
                safetyList.add(safety.asText());
            });
        }

        return safetyList.isEmpty() ? Collections.singletonList("No specific safety is provided") : safetyList;
    }

    // prompt for gemini
    private String createPromptForActivity(Activity activity) {
        return String.format(
                """
                                    Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
                        {
                            "analysis" :{
                                "overall" : "Overall analysis here",
                                "pace" : "Pace analysis here",
                                "heartRate" : "Heart rate analysis here",
                                "caloriedBurned" : "Calories analysis here"
                            },
                            "improvements" : [
                                {
                                    "area" : "Area name",
                                    "recommendations" : "Detailed recommendation"
                                }
                            ],
                            "suggestions": [
                                {
                                    "workout" : "Workout name",
                                    "description" : "Detailed Workout description"
                                }
                            ],
                            "safety" : [
                                "Safety point 1",
                                "Safety point 2"
                            ]
                        }

                        Analyze this activity:
                        Activity Type: %s
                        Duration: %d
                        Calories Burned: %d
                        Additional Metrics: %s

                        Provide detailed analysis focusing on performace, improvements, next workout suggestions, and safety guidelines.
                        Ensure the reponse follows the EXACT JSON format show above.
                        """,
                activity.getType(), activity.getDuration(), activity.getCaloriesBurned(),
                activity.getAddtionalMetrics());
    }

}
