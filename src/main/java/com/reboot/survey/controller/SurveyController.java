package com.reboot.survey.controller;

import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.Survey;
import com.reboot.survey.entity.enums.*;
import com.reboot.survey.service.SurveyService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/survey")
@SessionAttributes({"gameType", "skillLevel", "learningGoal", "availableTime"})
@Slf4j
public class SurveyController {

    private final SurveyService surveyService;
    private final MemberRepository memberRepository;

    public SurveyController(SurveyService surveyService, MemberRepository memberRepository) {
        this.surveyService = surveyService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/")
    public String showWelcomePage() {
        return "survey/welcome";
    }

    @GetMapping("/welcome")
    public String welcomePage() {
        return "survey/welcome"; // → templates/welcome.html 있어야 함
    }

    @GetMapping("/index")
    public String showIndexPage() {
        return "index"; // templates/index.html로 직접 리턴
    }

    @GetMapping("/start")
    public String startSurvey(HttpSession session) {
        // 새 설문 시작 시 세션 데이터 초기화
        session.removeAttribute("gameType");
        session.removeAttribute("skillLevel");
        session.removeAttribute("learningGoal");
        session.removeAttribute("availableTime");

        return "redirect:/survey/game-selection";
    }

    @GetMapping("/game-selection")
    public String gameSelectionForm() {
        return "survey/game-selection";
    }

    @PostMapping("/game-selection")
    public String processGameSelection(@RequestParam("gameType") GameType gameType, HttpSession session) {
        session.setAttribute("gameType", gameType);
        return "redirect:/survey/skill-level";
    }

    @GetMapping("/skill-level")
    public String skillLevelForm(HttpSession session) {
        if (session.getAttribute("gameType") == null) {
            return "redirect:/survey/game-selection";
        }
        return "survey/skill-level";
    }

    @PostMapping("/skill-level")
    public String processSkillLevel(@RequestParam("skillLevel") SkillLevel skillLevel, HttpSession session) {
        session.setAttribute("skillLevel", skillLevel);
        return "redirect:/survey/learning-goal";
    }

    @GetMapping("/learning-goal")
    public String learningGoalForm(HttpSession session) {
        if (session.getAttribute("skillLevel") == null) {
            return "redirect:/survey/skill-level";
        }
        return "survey/learning-goal";
    }

    @PostMapping("/learning-goal")
    public String processLearningGoal(@RequestParam("learningGoal") LearningGoal learningGoal, HttpSession session) {
        session.setAttribute("learningGoal", learningGoal);
        return "redirect:/survey/available-time";
    }

    @GetMapping("/available-time")
    public String availableTimeForm(HttpSession session) {
        if (session.getAttribute("learningGoal") == null) {
            return "redirect:/survey/learning-goal";
        }
        return "survey/available-time";
    }

    @PostMapping("/available-time")
    public String processAvailableTime(@RequestParam("availableTime") AvailableTime availableTime, HttpSession session) {
        session.setAttribute("availableTime", availableTime);
        return "redirect:/survey/lecture-preference";
    }

    @GetMapping("/lecture-preference")
    public String lecturePreferenceForm(HttpSession session) {
        if (session.getAttribute("availableTime") == null) {
            return "redirect:/survey/available-time";
        }
        return "survey/lecture-preference";
    }

    @PostMapping("/complete")
    public String completeSurvey(
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "lecturePreference") LecturePreference lecturePreference,
            @RequestParam(value = "gameTier", required = false) String gameTier,
            @RequestParam(value = "gamePosition", required = false) String gamePosition,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                // UserDetails에서 회원 ID 추출
                // memberId = ((CustomUserDetails) authentication.getPrincipal()).getMemberId();
            } else if (memberId == null) {
                // 로그인하지 않은 상태이고 memberId가 없을 경우 기본값 설정
                memberId = 1L; // 또는 에러 처리
            }

            // 세션에서 설문 데이터 가져오기
            GameType gameType = (GameType) session.getAttribute("gameType");
            SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
            LearningGoal learningGoal = (LearningGoal) session.getAttribute("learningGoal");
            AvailableTime availableTime = (AvailableTime) session.getAttribute("availableTime");

            // 필수 데이터 확인
            if (gameType == null || skillLevel == null || learningGoal == null || availableTime == null) {
                throw new IllegalStateException("필수 설문 데이터가 누락되었습니다.");
            }

            // SurveyRequest 객체 생성
            SurveyRequest surveyRequest = new SurveyRequest();
            surveyRequest.setMemberId(memberId);
            surveyRequest.setGameType(gameType);
            surveyRequest.setGameTier(gameTier);
            surveyRequest.setGamePosition(gamePosition);
            surveyRequest.setSkillLevel(skillLevel);
            surveyRequest.setLearningGoal(learningGoal);
            surveyRequest.setAvailableTime(availableTime);
            surveyRequest.setLecturePreference(lecturePreference);

            // 설문 제출 및 추천 결과 가져오기
            RecommendationResponse recommendationResponse = surveyService.submitSurveyAndGetRecommendations(memberId, surveyRequest);

            // 추천 결과를 세션에 저장
            session.setAttribute("recommendation", recommendationResponse);

            return "redirect:/survey/recommendation";
        } catch (Exception e) {
            log.error("설문 제출 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "설문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/survey/error";
        }
    }

    @GetMapping("/recommendation")
    public String showRecommendation(HttpSession session, Model model) {
        RecommendationResponse recommendation = (RecommendationResponse) session.getAttribute("recommendation");

        if (recommendation == null) {
            return "redirect:/survey/start";
        }

        model.addAttribute("recommendation", recommendation);
        return "survey/recommendation";
    }

    @GetMapping("/skip")
    public String skipSurvey(@RequestParam("step") String step) {
        return switch (step) {
            case "game-selection" -> "redirect:/survey/skill-level";
            case "skill-level" -> "redirect:/survey/learning-goal";
            case "learning-goal" -> "redirect:/survey/available-time";
            case "available-time" -> "redirect:/survey/lecture-preference";
            case "lecture-preference" -> "redirect:/survey/complete";
            default -> "redirect:/survey/start";
        };
    }

    @GetMapping("/error")
    public String showError() {
        return "survey/error";
    }
}