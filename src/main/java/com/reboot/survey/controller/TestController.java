package com.reboot.survey.controller;

import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.GameRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.enums.*;
import com.reboot.survey.service.SurveyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/survey")
@Slf4j
public class TestController {

    private final SurveyService surveyService;
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;

    @Autowired
    public TestController(SurveyService surveyService, MemberRepository memberRepository, GameRepository gameRepository) {
        this.surveyService = surveyService;
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
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
                    .append(", 역할: ").append(member.getRole());

            // 회원의 게임 정보 가져오기
            if (member.getGame() != null) {
                Game game = member.getGame();
                sb.append(", 게임 타입: ").append(game.getGameType())
                        .append(", 티어: ").append(game.getGameTier())
                        .append(", 포지션: ").append(game.getGamePosition());
            }

            sb.append("</li>");
        });

        sb.append("</ul>");
        return sb.toString();
    }

    @GetMapping("/survey-form")
    public String showSurveyForm(
            @RequestParam(value = "memberId", required = false) Long memberId,
            Model model) {

        // 기본값 설정
        Long defaultMemberId = 0L;
        String memberName = "홍길동";

        if (memberId != null && memberId > 0) {
            // memberId가 전달되면 해당 회원 정보 조회
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                defaultMemberId = member.getMemberId();
                memberName = member.getName();
                log.info("회원 정보 조회 성공 - ID: {}, 이름: {}", defaultMemberId, memberName);
            } else {
                log.warn("회원 ID {}를 찾을 수 없음. 기본값 사용", memberId);
            }
        } else {
            log.info("memberId가 없거나 유효하지 않음. 기본값 사용 - ID: {}, 이름: {}", defaultMemberId, memberName);
        }

        // 모델에 회원 정보 추가
        model.addAttribute("memberId", defaultMemberId);
        model.addAttribute("memberName", memberName);

        // Enum 값들을 모델에 추가
        model.addAttribute("gameTypes", GameType.values());
        model.addAttribute("skillLevels", SkillLevel.values());
        model.addAttribute("learningGoals", LearningGoal.values());
        model.addAttribute("availableTimes", AvailableTime.values());
        model.addAttribute("lecturePreferences", LecturePreference.values());

        // 게임별 티어와 포지션 정보 (JSON으로 전달)
        Map<String, Object> gameInfo = new HashMap<>();
        gameInfo.put("LOL", Map.of(
                "tiers", Arrays.asList("IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"),
                "positions", Arrays.asList("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")
        ));
        gameInfo.put("VALORANT", Map.of(
                "tiers", Arrays.asList("IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "ASCENDANT", "IMMORTAL", "RADIANT"),
                "positions", Arrays.asList("DUELIST", "SENTINEL", "INITIATOR", "CONTROLLER")
        ));
        gameInfo.put("TFT", Map.of(
                "tiers", Arrays.asList("IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"),
                "positions", Arrays.asList("ALL") // TFT는 포지션이 없으므로 ALL로 통일
        ));
        model.addAttribute("gameInfo", gameInfo);

        return "survey/survey-form";
    }

    @PostMapping("/submit-survey")
    public String submitSurvey(
            @RequestParam("gameType") GameType gameType,
            @RequestParam("skillLevel") SkillLevel skillLevel,
            @RequestParam("learningGoal") LearningGoal learningGoal,
            @RequestParam("availableTime") AvailableTime availableTime,
            @RequestParam("lecturePreference") LecturePreference lecturePreference,
            @RequestParam(value = "gameTier", required = false, defaultValue = "") String gameTier,
            @RequestParam(value = "gamePosition", required = false, defaultValue = "") String gamePosition,
            @RequestParam(value = "memberId", required = false) Long requestMemberId,
            Model model) {

        // memberId 처리 - 전달된 값이 null이거나 0보다 작으면 기본값 0 사용
        Long memberId = (requestMemberId != null && requestMemberId > 0) ? requestMemberId : 0L;

        try {
            log.info("설문 제출 시작 - memberId: {}, 게임: {}", memberId, gameType);

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

            // 설문 제출 및 결과 받기
            RecommendationResponse response = surveyService.submitSurveyAndGetRecommendations(memberId, request);

            if (response == null) {
                model.addAttribute("error", "추천 결과를 생성할 수 없습니다.");
                return "survey/error";
            }

            log.info("추천 결과 수신 - 추천 개수: {}",
                    response.getRecommendations() != null ? response.getRecommendations().size() : 0);

            // 모델에 결과 추가 (명시적으로 각 필드 추가)
            model.addAttribute("recommendation", response);

            return "survey/recommendation-result";
        } catch (Exception e) {
            log.error("설문 처리 중 오류 발생", e);
            model.addAttribute("error", "설문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "survey/error";
        }
    }

    @GetMapping("/start")
    public String startSurvey(@RequestParam(value = "memberId", required = false) Long memberId) {
        // memberId가 있으면 파라미터로 전달하여 설문 폼으로 이동
        if (memberId != null && memberId > 0) {
            return "redirect:/survey/survey-form?memberId=" + memberId;
        } else {
            // memberId가 없으면 기본값으로 이동
            return "redirect:/survey/survey-form";
        }
    }

    @GetMapping("/member/{memberId}")
    @ResponseBody
    public String getMemberGameInfo(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return "회원을 찾을 수 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(member.getName()).append("(").append(member.getNickname()).append(") 게임 정보</h2>");

        Game game = member.getGame();
        if (game != null) {
            sb.append("<p>게임 타입: ").append(game.getGameType()).append("</p>");
            sb.append("<p>티어: ").append(game.getGameTier()).append("</p>");
            sb.append("<p>포지션: ").append(game.getGamePosition()).append("</p>");
        } else {
            sb.append("<p>게임 정보가 없습니다.</p>");

            // 멤버 ID로 게임 정보 조회 (혹시 OneToOne 관계가 제대로 맺어지지 않은 경우)
            List<Game> games = gameRepository.findByMember_MemberId(memberId);
            if (!games.isEmpty()) {
                sb.append("<p>데이터베이스에서 찾은 게임 정보:</p>");
                for (Game g : games) {
                    sb.append("<p>게임 타입: ").append(g.getGameType()).append("</p>");
                    sb.append("<p>티어: ").append(g.getGameTier()).append("</p>");
                    sb.append("<p>포지션: ").append(g.getGamePosition()).append("</p>");
                }
            }
        }

        return sb.toString();
    }
}