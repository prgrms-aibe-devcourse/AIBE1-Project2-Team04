package com.reboot.survey.controller;

import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.enums.*;
import com.reboot.survey.service.UpdatedSurveyService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 설문조사 프로세스를 처리하는 컨트롤러입니다.
 */
@Controller
@RequestMapping("/survey")
public class UpdatedSurveyController {

    private final Logger log = LoggerFactory.getLogger(UpdatedSurveyController.class);
    private final UpdatedSurveyService surveyService;

    @Autowired
    public UpdatedSurveyController(UpdatedSurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /**
     * 설문 시작 페이지를 표시합니다.
     */
    @GetMapping("/welcome")
    public String showWelcomePage() {
        return "survey/welcome";
    }

    /**
     * 설문 프로세스를 시작하고 이전 세션 데이터를 정리합니다.
     */
    @GetMapping("/start")
    public String startSurvey(HttpSession session) {
        // 이전 설문 데이터가 있다면 세션에서 제거
        clearSessionData(session);
        return "redirect:/survey/game-selection";
    }

    /**
     * 게임 선택 페이지를 표시합니다.
     */
    @GetMapping("/game-selection")
    public String showGameSelection(Model model) {
        // GameType 열거형을 모델에 추가
        model.addAttribute("gameTypes", GameType.values());
        return "survey/game-selection";
    }

    /**
     * 게임 선택 결과를 처리합니다.
     */
    @PostMapping("/game-selection")
    public String processGameSelection(@RequestParam("gameType") GameType gameType, HttpSession session) {
        log.debug("게임 타입 선택: {}", gameType);
        session.setAttribute("gameType", gameType);
        return "redirect:/survey/skill-level";
    }

    /**
     * 실력 수준 선택 페이지를 표시합니다.
     */
    @GetMapping("/skill-level")
    public String showSkillLevel(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("gameType") == null) {
            return "redirect:/survey/game-selection";
        }

        // SkillLevel 열거형을 모델에 추가
        model.addAttribute("skillLevels", SkillLevel.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("skillLevel") != null) {
            model.addAttribute("selectedSkillLevel", session.getAttribute("skillLevel"));
        }

        return "survey/skill-level";
    }

    /**
     * 실력 수준 선택 결과를 처리합니다.
     */
    @PostMapping("/skill-level")
    public String processSkillLevel(@RequestParam("skillLevel") SkillLevel skillLevel, HttpSession session) {
        log.debug("실력 수준 선택: {}", skillLevel);
        session.setAttribute("skillLevel", skillLevel);
        return "redirect:/survey/learning-goal";
    }

    /**
     * 학습 목표 선택 페이지를 표시합니다.
     */
    @GetMapping("/learning-goal")
    public String showLearningGoal(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("skillLevel") == null) {
            return "redirect:/survey/skill-level";
        }

        // LearningGoal 열거형을 모델에 추가
        model.addAttribute("learningGoals", LearningGoal.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("learningGoal") != null) {
            model.addAttribute("selectedLearningGoal", session.getAttribute("learningGoal"));
        }

        return "survey/learning-goal";
    }

    /**
     * 학습 목표 선택 결과를 처리합니다.
     */
    @PostMapping("/learning-goal")
    public String processLearningGoal(@RequestParam("learningGoal") LearningGoal learningGoal, HttpSession session) {
        log.debug("학습 목표 선택: {}", learningGoal);
        session.setAttribute("learningGoal", learningGoal);
        return "redirect:/survey/available-time";
    }

    /**
     * 가능한 학습 시간 선택 페이지를 표시합니다.
     */
    @GetMapping("/available-time")
    public String showAvailableTime(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("learningGoal") == null) {
            return "redirect:/survey/learning-goal";
        }

        // AvailableTime 열거형을 모델에 추가
        model.addAttribute("availableTimes", AvailableTime.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("availableTime") != null) {
            model.addAttribute("selectedAvailableTime", session.getAttribute("availableTime"));
        }

        return "survey/available-time";
    }

    /**
     * 가능한 학습 시간 선택 결과를 처리합니다.
     */
    @PostMapping("/available-time")
    public String processAvailableTime(@RequestParam("availableTime") AvailableTime availableTime, HttpSession session) {
        log.debug("가능한 시간 선택: {}", availableTime);
        session.setAttribute("availableTime", availableTime);
        return "redirect:/survey/lecture-preference";
    }

    /**
     * 강의 선호도 선택 페이지를 표시합니다.
     */
    @GetMapping("/lecture-preference")
    public String showLecturePreference(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("availableTime") == null) {
            return "redirect:/survey/available-time";
        }

        // LecturePreference 열거형을 모델에 추가
        model.addAttribute("lecturePreferences", LecturePreference.values());
        
        return "survey/lecture-preference";
    }

    /**
     * 게임 관련 추가 정보 입력 페이지를 표시합니다 (게임 타입에 따라 다름).
     */
    @GetMapping("/game-details")
    public String showGameDetails(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("lecturePreference") == null) {
            return "redirect:/survey/lecture-preference";
        }

        // 게임 타입에 따라 다른 정보 요청
        GameType gameType = (GameType) session.getAttribute("gameType");
        model.addAttribute("gameType", gameType);
        
        // 게임별 티어 및 포지션 정보 제공
        switch (gameType) {
            case LOL:
                model.addAttribute("tiers", new String[]{"IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"});
                model.addAttribute("positions", new String[]{"TOP", "JUNGLE", "MID", "ADC", "SUPPORT"});
                break;
            case VALORANT:
                model.addAttribute("tiers", new String[]{"IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "ASCENDANT", "IMMORTAL", "RADIANT"});
                model.addAttribute("positions", new String[]{"DUELIST", "SENTINEL", "INITIATOR", "CONTROLLER"});
                break;
            case TFT:
                model.addAttribute("tiers", new String[]{"IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"});
                // TFT는 포지션 없음
                break;
            default:
                // 다른 게임들
                model.addAttribute("tiers", new String[]{});
                model.addAttribute("positions", new String[]{});
        }

        return "survey/game-details";
    }

    /**
     * 설문 프로세스 완료 및 추천 생성을 처리합니다.
     */
    @PostMapping("/complete")
    public String completeSurvey(
            HttpSession session,
            @RequestParam("lecturePreference") LecturePreference lecturePreference,
            @RequestParam("memberId") Long memberId,
            @RequestParam(value = "gameTier", required = false) String gameTier,
            @RequestParam(value = "gamePosition", required = false) String gamePosition,
            Model model) {

        // 세션에서 설문 데이터 가져오기
        GameType gameType = (GameType) session.getAttribute("gameType");
        SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
        LearningGoal learningGoal = (LearningGoal) session.getAttribute("learningGoal");
        AvailableTime availableTime = (AvailableTime) session.getAttribute("availableTime");

        // 필수 정보가 없는 경우 처리
        if (gameType == null || skillLevel == null || learningGoal == null ||
                availableTime == null || lecturePreference == null) {
            log.warn("설문 데이터 불완전: gameType={}, skillLevel={}, learningGoal={}, availableTime={}, lecturePreference={}",
                    gameType, skillLevel, learningGoal, availableTime, lecturePreference);
            return "redirect:/survey/start";
        }

        // 설문 요청 객체 생성
        SurveyRequest surveyRequest = createSurveyRequest(
                memberId, gameType, skillLevel, learningGoal, availableTime, 
                lecturePreference, gameTier, gamePosition);

        try {
            // 설문 제출 및 강의 추천 받기
            RecommendationResponse recommendations =
                    surveyService.submitSurveyAndGetRecommendations(memberId, surveyRequest);

            // 추천 결과를 모델에 추가
            model.addAttribute("recommendedLectures", recommendations.getRecommendations());
            model.addAttribute("recommendationReasons", recommendations.getOverallRecommendation());

            // 세션 정리
            clearSessionData(session);

            return "survey/recommendation";
        } catch (Exception e) {
            // 오류 처리
            log.error("추천 생성 중 오류: {}", e.getMessage(), e);
            model.addAttribute("error", "추천 과정에서 오류가 발생했습니다: " + e.getMessage());
            return "survey/error";
        }
    }

    /**
     * 설문 프로세스를 건너뛰고 홈으로 이동합니다.
     */
    @GetMapping("/home")
    public String skipSurvey(HttpSession session) {
        clearSessionData(session);
        return "redirect:/";
    }

    /**
     * 설문 데이터를 세션에서 정리합니다.
     */
    private void clearSessionData(HttpSession session) {
        session.removeAttribute("gameType");
        session.removeAttribute("skillLevel");
        session.removeAttribute("learningGoal");
        session.removeAttribute("availableTime");
        session.removeAttribute("lecturePreference");
    }

    /**
     * 설문 요청 객체를 생성합니다.
     */
    private SurveyRequest createSurveyRequest(
            Long memberId, GameType gameType, SkillLevel skillLevel, 
            LearningGoal learningGoal, AvailableTime availableTime, 
            LecturePreference lecturePreference, String gameTier, String gamePosition) {
        
        SurveyRequest request = new SurveyRequest();
        request.setMemberId(memberId);
        request.setGameType(gameType);
        request.setSkillLevel(skillLevel);
        request.setLearningGoal(learningGoal);
        request.setAvailableTime(availableTime);
        request.setLecturePreference(lecturePreference);
        request.setGameTier(gameTier);
        request.setGamePosition(gamePosition);
        
        return request;
    }
}