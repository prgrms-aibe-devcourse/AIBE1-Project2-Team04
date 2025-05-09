package com.reboot.survey.config;

import com.reboot.lecture.repository.LectureRepository;
import com.reboot.survey.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SurveyConfig {

    /**
     * 간소화된 LLM 클라이언트를 제공합니다.
     */
    @Bean
    @Primary
    public LlmClient simplifiedLlmClient() {
        return new SimplifiedLlmClientImpl();
    }
    
    /**
     * 간소화된 추천 서비스를 제공합니다.
     */
    @Bean
    @Primary
    public SimplifiedRecommendationService simplifiedRecommendationService(
            LectureRepository lectureRepository,
            LlmClient llmClient) {
        return new SimplifiedRecommendationService(lectureRepository, llmClient);
    }
}