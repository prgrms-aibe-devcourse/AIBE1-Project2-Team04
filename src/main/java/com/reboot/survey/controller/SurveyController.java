package com.reboot.survey.controller;

import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
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
        return "survey/welcome";
    }

    @GetMapping("/index")
    public String showIndexPage() {
        return "index";
    }

    @GetMapping("/start")
    public String startSurvey(HttpSession session) {
        // 새 설문 시작 시 세션 데이터 초기화
        session.removeAttribute("gameType");
        session.removeAttribute("skillLevel");
        session.removeAttribute("learningGoal");
        session.removeAttribute("availableTime");
        session.removeAttribute("gameTier");
        session.removeAttribute("gamePosition");

        return "redirect:/survey/game-selection";
    }

    @GetMapping("/game-selection")
    public String gameSelectionForm() {
        return "survey/game-selection";
    }

    @PostMapping("/game-selection")
    public String processGameSelection(
            @RequestParam("gameType") GameType gameType,
            @RequestParam(value = "gameTier", required = false) String gameTier,
            @RequestParam(value = "gamePosition", required = false) String gamePosition,
            HttpSession session) {

        session.setAttribute("gameType", gameType);

        // 티어와 포지션 정보가 있다면 세션에 저장
        if (gameTier != null && !gameTier.isEmpty()) {
            session.setAttribute("gameTier", gameTier);
        }
        if (gamePosition != null && !gamePosition.isEmpty()) {
            session.setAttribute("gamePosition", gamePosition);
        }

        log.info("게임 선택 완료 - 게임: {}, 티어: {}, 포지션: {}", gameType, gameTier, gamePosition);

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
            @RequestParam(value = "lecturePreference") LecturePreference lecturePreference,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            // 현재 인증된 사용자 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long memberId = null;

            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                // 로그인한 사용자의 경우 UserDetails에서 회원 ID 추출
                // UserDetails에서 memberId를 가져오는 방식은 구현에 따라 달라질 수 있음
                // memberId = ((CustomUserDetails) authentication.getPrincipal()).getMemberId();

                // 임시로 첫 번째 회원 ID 사용 (실제 구현 시 수정 필요)
                Member member = memberRepository.findAll().stream().findFirst().orElse(null);
                if (member != null) {
                    memberId = member.getMemberId();
                }
            } else {
                // 로그인하지 않은 경우 기본 회원 ID 사용
                Member defaultMember = memberRepository.findAll().stream().findFirst().orElse(null);
                if (defaultMember != null) {
                    memberId = defaultMember.getMemberId();
                } else {
                    // 회원이 없으면 기본값 사용
                    memberId = 1L;
                }
            }

            // 세션에서 설문 데이터 가져오기
            GameType gameType = (GameType) session.getAttribute("gameType");
            SkillLevel skillLevel = (SkillLevel) session.getAttribute("skillLevel");
            LearningGoal learningGoal = (LearningGoal) session.getAttribute("learningGoal");
            AvailableTime availableTime = (AvailableTime) session.getAttribute("availableTime");
            String gameTier = (String) session.getAttribute("gameTier");
            String gamePosition = (String) session.getAttribute("gamePosition");

            // 디버깅 로그 추가
            log.info("설문 완료 - 세션에서 가져온 데이터:");
            log.info(" - gameType: {}", gameType);
            log.info(" - gameTier: {}", gameTier);
            log.info(" - gamePosition: {}", gamePosition);
            log.info(" - skillLevel: {}", skillLevel);
            log.info(" - learningGoal: {}", learningGoal);
            log.info(" - availableTime: {}", availableTime);
            log.info(" - lecturePreference: {}", lecturePreference);

            // 필수 데이터 확인
            if (gameType == null || skillLevel == null || learningGoal == null || availableTime == null) {
                throw new IllegalStateException("필수 설문 데이터가 누락되었습니다.");
            }

            log.info("설문 제출 시작 - memberId: {}, 게임: {}, 티어: {}, 포지션: {}",
                    memberId, gameType, gameTier, gamePosition);

            // SurveyRequest 객체 생성
            SurveyRequest surveyRequest = new SurveyRequest();
            surveyRequest.setMemberId(memberId);
            surveyRequest.setGameType(gameType);
            surveyRequest.setGameTier(gameTier != null && !gameTier.isEmpty() ? gameTier : null);
            surveyRequest.setGamePosition(gamePosition != null && !gamePosition.isEmpty() ? gamePosition : null);
            surveyRequest.setSkillLevel(skillLevel);
            surveyRequest.setLearningGoal(learningGoal);
            surveyRequest.setAvailableTime(availableTime);
            surveyRequest.setLecturePreference(lecturePreference);

            // 설문 제출 및 추천 결과 가져오기
            RecommendationResponse recommendationResponse = surveyService.submitSurveyAndGetRecommendations(memberId, surveyRequest);

            if (recommendationResponse == null) {
                model.addAttribute("error", "추천 결과를 생성할 수 없습니다.");
                return "survey/error";
            }

            log.info("추천 결과 수신 - 추천 개수: {}",
                    recommendationResponse.getRecommendations() != null ? recommendationResponse.getRecommendations().size() : 0);

            // 추천 결과를 세션에 저장
            session.setAttribute("recommendation", recommendationResponse);

            return "redirect:/survey/recommendation";
        } catch (Exception e) {
            log.error("설문 제출 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "설문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "survey/error";
        }
    }

    @GetMapping("/recommendation")
    public String showRecommendation(HttpSession session, Model model) {
        RecommendationResponse recommendation = (RecommendationResponse) session.getAttribute("recommendation");

        if (recommendation == null) {
            log.warn("세션에 추천 결과가 없어 시작 페이지로 리다이렉트");
            return "redirect:/survey/start";
        }

        model.addAttribute("recommendation", recommendation);
        return "survey/recommendation";
    }

    @GetMapping("/error")
    public String showError() {
        return "survey/error";
    }
}