package com.reboot.survey.controller;

import com.reboot.auth.entity.Member;
import com.reboot.auth.security.CustomUserDetails;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * 설문조사를 제출하고 즉시 강의 추천을 받습니다.
     */
    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> submitSurveyAndGetRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SurveyRequest surveyRequest) {

        // 현재 로그인한 사용자의 회원 ID 가져오기
        Long memberId = ((CustomUserDetails) userDetails).getMemberId();

        // 설문조사 제출 및 추천 생성
        RecommendationResponse recommendations =
                surveyService.submitSurveyAndGetRecommendations(memberId, surveyRequest);

        return ResponseEntity.ok(recommendations);
    }
}