/*
package com.reboot.survey.controller;

import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.enums.*;
import com.reboot.survey.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;

    @Autowired
    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping("/welcome")
    public String showWelcomePage() {
        return "survey/welcome";
    }

    @GetMapping("/start")
    public String startSurvey() {
        return "survey/game-selection";
    }

    @GetMapping("/game-selection")
    public String showGameSelection() {
        return "survey/game-selection";
    }

    @PostMapping("/game-selection")
    public String processGameSelection(@RequestParam("gameType") GameType gameType, Model model) {
        model.addAttribute("gameType", gameType);
        return "redirect:/survey/skill-level";
    }

    @GetMapping("/skill-level")
    public String showSkillLevel() {
        return "survey/skill-level";
    }

    @PostMapping("/skill-level")
    public String processSkillLevel(@RequestParam("skillLevel") SkillLevel skillLevel, Model model) {
        model.addAttribute("skillLevel", skillLevel);
        return "redirect:/survey/learning-goal";
    }

    @GetMapping("/learning-goal")
    public String showLearningGoal() {
        return "survey/learning-goal";
    }

    @PostMapping("/learning-goal")
    public String processLearningGoal(@RequestParam("learningGoal") LearningGoal learningGoal, Model model) {
        model.addAttribute("learningGoal", learningGoal);
        return "redirect:/survey/available-time";
    }

    @GetMapping("/available-time")
    public String showAvailableTime() {
        return "survey/available-time";
    }

    @PostMapping("/available-time")
    public String processAvailableTime(@RequestParam("availableTime") AvailableTime availableTime, Model model) {
        model.addAttribute("availableTime", availableTime);
        return "redirect:/survey/lecture-preference";
    }

    @GetMapping("/lecture-preference")
    public String showLecturePreference() {
        return "survey/lecture-preference";
    }

    @PostMapping("/complete")
    public String completeSurvey(
            @SessionAttribute(name = "gameType", required = false) GameType gameType,
            @SessionAttribute(name = "skillLevel", required = false) SkillLevel skillLevel,
            @SessionAttribute(name = "learningGoal", required = false) LearningGoal learningGoal,
            @SessionAttribute(name = "availableTime", required = false) AvailableTime availableTime,
            @RequestParam("lecturePreference") LecturePreference lecturePreference,
            @RequestBody SurveyRequest surveyRequest,
            Model model) {

        // 현재 로그인한 회원 정보 가져오기
        Long memberId = surveyRequest.getMemberId();

        // 설문 요청 객체 생성
        surveyRequest = new SurveyRequest();
        surveyRequest.setMemberId(memberId);
        surveyRequest.setGameType(gameType);
        surveyRequest.setSkillLevel(skillLevel);
        surveyRequest.setLearningGoal(learningGoal);
        surveyRequest.setAvailableTime(availableTime);
        surveyRequest.setLecturePreference(lecturePreference);

        // 설문 제출 및 강의 추천 받기
        RecommendationResponse recommendations =
                surveyService.submitSurveyAndGetRecommendations(memberId, surveyRequest);

        // 추천 결과를 모델에 추가
        model.addAttribute("recommendedLectures", recommendations.getRecommendations());
        model.addAttribute("recommendationReasons", recommendations.getOverallRecommendation());

        return "survey/recommendation";
    }

    @GetMapping("/home")
    public String skipSurvey() {
        return "index";
    }
}*/
