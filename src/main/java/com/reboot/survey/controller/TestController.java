package com.reboot.survey.controller;

import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.enums.*;
import com.reboot.survey.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/test")
public class TestController {

    private final SurveyService surveyService;
    private final MemberRepository memberRepository;

    @Autowired
    public TestController(SurveyService surveyService, MemberRepository memberRepository) {
        this.surveyService = surveyService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/members")
    @ResponseBody
    public String listMembers() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>사용 가능한 회원 목록</h1>");
        sb.append("<ul>");

        memberRepository.findAll().forEach(member -> {
            sb.append("<li>")
                    .append("ID: ").append(member.getMemberId())
                    .append(", 이름: ").append(member.getName())
                    .append(", 닉네임: ").append(member.getNickname())
                    .append(", 역할: ").append(member.getRole())
                    .append("</li>");
        });

        sb.append("</ul>");
        return sb.toString();
    }

    @GetMapping("/survey-form")
    public String showSurveyForm(Model model) {
        // 첫 번째 멤버 ID를 기본값으로 사용
        Optional<Member> firstMember = memberRepository.findById(1L);

        if (firstMember.isPresent()) {
            model.addAttribute("memberId", firstMember.get().getMemberId());
            model.addAttribute("memberName", firstMember.get().getName());
        } else {
            model.addAttribute("memberId", 1L);
            model.addAttribute("memberName", "테스트 사용자");
        }

        // Enum 값들을 모델에 추가
        model.addAttribute("gameTypes", GameType.values());
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("learningGoals", LearningGoal.values());
        model.addAttribute("availableTimes", AvailableTime.values());
        model.addAttribute("lecturePreferences", LecturePreference.values());

        return "test/survey-form";
    }

    @PostMapping("/submit-survey")
    public String submitSurvey(
            @RequestParam("memberId") Long memberId,
            @RequestParam("gameType") GameType gameType,
            @RequestParam("skillLevel") SkillLevel skillLevel,
            @RequestParam("learningGoal") LearningGoal learningGoal,
            @RequestParam("availableTime") AvailableTime availableTime,
            @RequestParam("lecturePreference") LecturePreference lecturePreference,
            @RequestParam(value = "gameTier", required = false, defaultValue = "") String gameTier,
            @RequestParam(value = "gamePosition", required = false, defaultValue = "") String gamePosition,
            Model model) {

        // SurveyRequest 객체 생성
        SurveyRequest request = new SurveyRequest();
        request.setMemberId(memberId);
        request.setGameType(gameType);
        request.setGameTier(gameTier);
        request.setGamePosition(gamePosition);
        request.setSkillLevel(skillLevel);
        request.setLearningGoal(learningGoal);
        request.setAvailableTime(availableTime);
        request.setLecturePreference(lecturePreference);

        try {
            // 설문 제출 및 결과 받기
            RecommendationResponse response = surveyService.submitSurveyAndGetRecommendations(memberId, request);

            // 결과를 모델에 추가
            model.addAttribute("recommendation", response);

            return "test/recommendation-result";
        } catch (Exception e) {
            model.addAttribute("error", "설문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "survey/error";
        }
    }
}