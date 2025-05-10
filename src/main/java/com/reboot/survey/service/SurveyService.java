package com.reboot.survey.service;

import com.reboot.auth.entity.Game;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.Survey;
import com.reboot.survey.repository.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class SurveyService {

    private final Logger log = LoggerFactory.getLogger(SurveyService.class);
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final LectureRecommendationService lectureRecommendationService;

    @Autowired
    public SurveyService(
            SurveyRepository surveyRepository,
            MemberRepository memberRepository,
            LectureRecommendationService lectureRecommendationService) {
        this.surveyRepository = surveyRepository;
        this.memberRepository = memberRepository;
        this.lectureRecommendationService = lectureRecommendationService;
    }

    /**
     * 사용자의 설문조사를 저장하고 즉시 강의 추천을 제공합니다.
     *
     * @param memberId      회원 ID
     * @param surveyRequest 설문조사 요청 DTO
     * @return 추천된 강의 목록
     */
    public RecommendationResponse submitSurveyAndGetRecommendations(Long memberId, SurveyRequest surveyRequest) {
        log.info("설문 제출 시작 - 회원 ID: {}", memberId);

        // 1. 회원 정보 조회 (없으면 더미 회원 생성)
        Member member;
        try {
            member = memberRepository.findById(memberId)
                    .orElseGet(() -> {
                        log.warn("회원을 찾을 수 없습니다: {}. 더미 회원 생성.", memberId);
                        Member dummy = new Member();
                        dummy.setMemberId(memberId);
                        dummy.setName("테스트 사용자");
                        dummy.setNickname("테스트유저");

                        // 설문에서 선택한 게임 정보로 더미 Game 객체 생성
                        if (surveyRequest.getGameType() != null) {
                            Game game = new Game();
                            game.setMember(dummy);
                            game.setGameType(surveyRequest.getGameType().name());
                            game.setGameTier(surveyRequest.getGameTier());
                            game.setGamePosition(surveyRequest.getGamePosition());
                            dummy.setGame(game);
                        }

                        return dummy;
                    });
        } catch (Exception e) {
            log.error("회원 정보 조회 실패: {}", e.getMessage());
            // 더미 회원 생성
            Member dummy = new Member();
            dummy.setMemberId(memberId);
            dummy.setName("테스트 사용자");
            dummy.setNickname("테스트유저");

            // 설문에서 선택한 게임 정보 설정
            Game game = new Game();
            game.setMember(dummy);
            game.setGameType(surveyRequest.getGameType().name());
            game.setGameTier(surveyRequest.getGameTier());
            game.setGamePosition(surveyRequest.getGamePosition());
            dummy.setGame(game);

            member = dummy;
        }

        // 설문 정보에서 게임 정보 설정 (회원에 게임 정보가 없는 경우)
        if (member.getGame() == null && surveyRequest.getGameType() != null) {
            Game game = new Game();
            game.setMember(member);
            game.setGameType(surveyRequest.getGameType().name());
            game.setGameTier(surveyRequest.getGameTier());
            game.setGamePosition(surveyRequest.getGamePosition());
            member.setGame(game);
        }

        // 2. 설문조사 엔티티 생성 및 저장
        Survey survey = new Survey();
        survey.setMember(member);
        survey.setGameType(surveyRequest.getGameType());
        survey.setGameTier(surveyRequest.getGameTier());
        survey.setGamePosition(surveyRequest.getGamePosition());
        survey.setSkillLevel(surveyRequest.getSkillLevel());
        survey.setLearningGoal(surveyRequest.getLearningGoal());
        survey.setAvailableTime(surveyRequest.getAvailableTime());
        survey.setLecturePreference(surveyRequest.getLecturePreference());

        Survey savedSurvey;
        try {
            // 테스트 모드에서는 저장 없이 진행
            if (memberRepository.existsById(memberId)) {
                savedSurvey = surveyRepository.save(survey);
                log.info("설문 저장 성공 - 설문 ID: {}", savedSurvey.getId());
            } else {
                log.info("테스트 모드: 실제 저장 없이 진행합니다.");
                savedSurvey = survey; // 저장 없이 진행
                // 설문 ID를 임시로 설정
                savedSurvey.setId(1L);
            }
        } catch (Exception e) {
            log.error("설문 저장 실패: {}", e.getMessage());
            savedSurvey = survey; // 실패해도 저장 없이 진행
            log.info("저장 실패하여 저장 없이 진행합니다.");
            // 설문 ID를 임시로 설정
            savedSurvey.setId(1L);
        }

        // 3. 강의 추천 실행
        try {
            RecommendationResponse recommendations = lectureRecommendationService.getRecommendations(member, savedSurvey);
            log.info("강의 추천 성공 - 추천 개수: {}",
                    recommendations.getRecommendations() != null ? recommendations.getRecommendations().size() : 0);
            return recommendations;
        } catch (Exception e) {
            log.error("강의 추천 생성 실패: {}", e.getMessage());

            // 오류 발생시 빈 응답 객체 반환
            RecommendationResponse emptyResponse = new RecommendationResponse();
            emptyResponse.setSurveyId(savedSurvey.getId());
            emptyResponse.setOverallRecommendation("강의 추천 생성 중 오류가 발생했습니다. 나중에 다시 시도해주세요.");
            emptyResponse.setRecommendations(new ArrayList<>());
            return emptyResponse;
        }
    }
}