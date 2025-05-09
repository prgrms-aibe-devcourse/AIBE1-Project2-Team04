package com.reboot.survey.controller;

import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.enums.*;
import com.reboot.survey.service.FinalSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/survey")
public class FinalSurveyController {

    private final Logger log = LoggerFactory.getLogger(FinalSurveyController.class);
    private final FinalSurveyService surveyService;

    @Autowired
    public FinalSurveyController(FinalSurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping("/welcome")
    public String showWelcomePage() {
        return "survey/welcome";
    }

    @GetMapping("/start")
    public String startSurvey(HttpSession session) {
        // 이전 설문 데이터가 있다면 세션에서 제거
        session.removeAttribute("gameType");
        session.removeAttribute("skillLevel");
        session.removeAttribute("learningGoal");
        session.removeAttribute("availableTime");
        return "redirect:/survey/game-selection";
    }

    @GetMapping("/game-selection")
    public String showGameSelection(Model model) {
        // 게임 타입 목록을 모델에 추가
        model.addAttribute("gameTypes", GameType.values());
        return "survey/game-selection";
    }

    @PostMapping("/game-selection")
    public String processGameSelection(@RequestParam("gameType") GameType gameType, HttpSession session) {
        log.info("선택된 게임 타입: {}", gameType.name());
        session.setAttribute("gameType", gameType);
        return "redirect:/survey/skill-level";
    }

    @GetMapping("/skill-level")
    public String showSkillLevel(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("gameType") == null) {
            return "redirect:/survey/game-selection";
        }

        // 현재 선택된 게임 타입 모델에 추가
        GameType gameType = (GameType) session.getAttribute("gameType");
        model.addAttribute("selectedGameType", gameType);
        
        // 스킬 레벨 목록 모델에 추가
        model.addAttribute("skillLevels", SkillLevel.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("skillLevel") != null) {
            model.addAttribute("selectedSkillLevel", session.getAttribute("skillLevel"));
        }

        return "survey/skill-level";
    }

    @PostMapping("/skill-level")
    public String processSkillLevel(@RequestParam("skillLevel") SkillLevel skillLevel, HttpSession session) {
        log.info("선택된 스킬 레벨: {}", skillLevel.name());
        session.setAttribute("skillLevel", skillLevel);
        return "redirect:/survey/learning-goal";
    }

    @GetMapping("/learning-goal")
    public String showLearningGoal(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("skillLevel") == null) {
            return "redirect:/survey/skill-level";
        }

        // 현재까지 선택한 데이터 모델에 추가
        GameType gameType = (GameType) session.getAttribute("gameType");
        SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
        
        model.addAttribute("selectedGameType", gameType);
        model.addAttribute("selectedSkillLevel", skillLevel);
        
        // 학습 목표 목록 모델에 추가
        model.addAttribute("learningGoals", LearningGoal.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("learningGoal") != null) {
            model.addAttribute("selectedLearningGoal", session.getAttribute("learningGoal"));
        }

        return "survey/learning-goal";
    }

    @PostMapping("/learning-goal")
    public String processLearningGoal(@RequestParam("learningGoal") LearningGoal learningGoal, HttpSession session) {
        log.info("선택된 학습 목표: {}", learningGoal.name());
        session.setAttribute("learningGoal", learningGoal);
        return "redirect:/survey/available-time";
    }

    @GetMapping("/available-time")
    public String showAvailableTime(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("learningGoal") == null) {
            return "redirect:/survey/learning-goal";
        }

        // 현재까지 선택한 데이터 모델에 추가
        GameType gameType = (GameType) session.getAttribute("gameType");
        SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
        LearningGoal learningGoal = (LearningGoal) session.getAttribute("learningGoal");
        
        model.addAttribute("selectedGameType", gameType);
        model.addAttribute("selectedSkillLevel", skillLevel);
        model.addAttribute("selectedLearningGoal", learningGoal);
        
        // 가능한 시간 목록 모델에 추가
        model.addAttribute("availableTimes", AvailableTime.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("availableTime") != null) {
            model.addAttribute("selectedAvailableTime", session.getAttribute("availableTime"));
        }

        return "survey/available-time";
    }

    @PostMapping("/available-time")
    public String processAvailableTime(@RequestParam("availableTime") AvailableTime availableTime, HttpSession session) {
        log.info("선택된 가능 시간: {}", availableTime.name());
        session.setAttribute("availableTime", availableTime);
        return "redirect:/survey/game-detail";
    }
    
    @GetMapping("/game-detail")
    public String showGameDetail(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("availableTime") == null) {
            return "redirect:/survey/available-time";
        }
        
        // 현재까지 선택한 데이터 모델에 추가
        GameType gameType = (GameType) session.getAttribute("gameType");
        model.addAttribute("selectedGameType", gameType);
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("gameTier") != null) {
            model.addAttribute("selectedGameTier", session.getAttribute("gameTier"));
        }
        
        if (session.getAttribute("gamePosition") != null) {
            model.addAttribute("selectedGamePosition", session.getAttribute("gamePosition"));
        }
        
        // 게임별 티어와 포지션 정보 (예시)
        if (gameType == GameType.LOL) {
            model.addAttribute("tiers", new String[]{"아이언", "브론즈", "실버", "골드", "플래티넘", "다이아몬드", "마스터", "그랜드마스터", "챌린저"});
            model.addAttribute("positions", new String[]{"탑", "정글", "미드", "봇", "서포터"});
        } else if (gameType == GameType.OVERWATCH) {
            model.addAttribute("tiers", new String[]{"브론즈", "실버", "골드", "플래티넘", "다이아몬드", "마스터", "그랜드마스터", "TOP 500"});
            model.addAttribute("positions", new String[]{"탱커", "딜러", "서포터"});
        } else if (gameType == GameType.VALORANT) {
            model.addAttribute("tiers", new String[]{"아이언", "브론즈", "실버", "골드", "플래티넘", "다이아몬드", "불멸", "레디언트"});
            model.addAttribute("positions", new String[]{"듀얼리스트", "센티널", "컨트롤러", "이니시에이터"});
        } else {
            // 기본 값
            model.addAttribute("tiers", new String[]{});
            model.addAttribute("positions", new String[]{});
        }
        
        return "survey/game-detail";
    }
    
    @PostMapping("/game-detail")
    public String processGameDetail(
            @RequestParam(value = "gameTier", required = false) String gameTier,
            @RequestParam(value = "gamePosition", required = false) String gamePosition,
            HttpSession session) {
        
        log.info("선택된 게임 티어: {}, 포지션: {}", gameTier, gamePosition);
        
        if (gameTier != null && !gameTier.isEmpty()) {
            session.setAttribute("gameTier", gameTier);
        }
        
        if (gamePosition != null && !gamePosition.isEmpty()) {
            session.setAttribute("gamePosition", gamePosition);
        }
        
        return "redirect:/survey/lecture-preference";
    }

    @GetMapping("/lecture-preference")
    public String showLecturePreference(HttpSession session, Model model) {
        // 이전 단계 데이터 확인
        if (session.getAttribute("availableTime") == null) {
            return "redirect:/survey/available-time";
        }
        
        // 현재까지 선택한 데이터 모델에 추가
        GameType gameType = (GameType) session.getAttribute("gameType");
        SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
        LearningGoal learningGoal = (LearningGoal) session.getAttribute("learningGoal");
        AvailableTime availableTime = (AvailableTime) session.getAttribute("availableTime");
        
        model.addAttribute("selectedGameType", gameType);
        model.addAttribute("selectedSkillLevel", skillLevel);
        model.addAttribute("selectedLearningGoal", learningGoal);
        model.addAttribute("selectedAvailableTime", availableTime);
        
        // 선택된 게임 상세 정보 추가
        if (session.getAttribute("gameTier") != null) {
            model.addAttribute("selectedGameTier", session.getAttribute("gameTier"));
        }
        
        if (session.getAttribute("gamePosition") != null) {
            model.addAttribute("selectedGamePosition", session.getAttribute("gamePosition"));
        }
        
        // 강의 선호도 목록 모델에 추가
        model.addAttribute("lecturePreferences", LecturePreference.values());
        
        // 이전에 선택한 값이 있으면 모델에 추가
        if (session.getAttribute("lecturePreference") != null) {
            model.addAttribute("selectedLecturePreference", session.getAttribute("lecturePreference"));
        }
        
        return "survey/lecture-preference";
    }

    @PostMapping("/complete")
    public String completeSurvey(
            HttpSession session,
            @RequestParam("lecturePreference") LecturePreference lecturePreference,
            @RequestParam("memberId") Long memberId,
            Model model) {

        log.info("설문 완료 요청 - 회원 ID: {}, 강의 선호도: {}", memberId, lecturePreference.name());
        
        // 세션에서 설문 데이터 가져오기
        GameType gameType = (GameType) session.getAttribute("gameType");
        SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
        LearningGoal learningGoal = (LearningGoal) session.getAttribute("learningGoal");
        AvailableTime availableTime = (AvailableTime) session.getAttribute("availableTime");
        String gameTier = (String) session.getAttribute("gameTier");
        String gamePosition = (String) session.getAttribute("gamePosition");

        // 필수 정보가 없는 경우 처리
        if (gameType == null || skillLevel == null || learningGoal == null ||
                availableTime == null || lecturePreference == null) {
            log.error("필수 설문 데이터 누락");
            return "redirect:/survey/start";
        }

        // 설문 요청 객체 생성
        SurveyRequest surveyRequest = new SurveyRequest();
        surveyRequest.setMemberId(memberId);
        surveyRequest.setGameType(gameType);
        surveyRequest.setSkillLevel(skillLevel);
        surveyRequest.setLearningGoal(learningGoal);
        surveyRequest.setAvailableTime(availableTime);
        surveyRequest.setLecturePreference(lecturePreference);
        surveyRequest.setGameTier(gameTier);
        surveyRequest.setGamePosition(gamePosition);

        try {
            log.info("추천 서비스 호출 시작");
            // 설문 제출 및 강의 추천 받기
            RecommendationResponse recommendations =
                    surveyService.submitSurveyAndGetRecommendations(memberId, surveyRequest);

            log.info("추천 서비스 호출 완료 - 추천 수: {}", 
                    recommendations.getRecommendations() != null ? recommendations.getRecommendations().size() : 0);
            
            // 추천 결과를 모델에 추가
            model.addAttribute("recommendedLectures", recommendations.getRecommendations());
            model.addAttribute("recommendationReasons", recommendations.getOverallRecommendation());
            model.addAttribute("surveyRequest", surveyRequest); // 설문 정보도 추가

            // 세션 정리
            session.removeAttribute("gameType");
            session.removeAttribute("skillLevel");
            session.removeAttribute("learningGoal");
            session.removeAttribute("availableTime");
            session.removeAttribute("gameTier");
            session.removeAttribute("gamePosition");
            session.removeAttribute("lecturePreference");

            return "survey/recommendation";
        } catch (Exception e) {
            // 오류 처리
            log.error("추천 프로세스 오류: {}", e.getMessage(), e);
            model.addAttribute("error", "추천 과정에서 오류가 발생했습니다: " + e.getMessage());
            return "survey/error";
        }
    }

    @GetMapping("/home")
    public String skipSurvey() {
        return "redirect:/";
    }
}