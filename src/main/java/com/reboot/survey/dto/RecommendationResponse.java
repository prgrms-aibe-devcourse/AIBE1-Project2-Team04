package com.reboot.survey.dto;

import com.reboot.survey.service.LectureRecommendation;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationResponse {
    private Long surveyId;
    private List<LectureRecommendation> recommendations;
    private String overallRecommendation;
}