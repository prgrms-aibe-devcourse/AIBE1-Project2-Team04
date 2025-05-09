package com.reboot.survey.service;

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

import java.util.List;

/**
 * 설문 서비스는 사용자의 설문 데이터를 저장하고 강의 추천을 제공합니다.
 */
@Service
@Transactional
public class UpdatedSurveyService {

    private final Logger log = LoggerFactory.getLogger(UpdatedSurveyService.class);
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final SimplifiedRecommendationService recommendationService;

    @Autowired
    public UpdatedSurveyService(
            SurveyRepository surveyRepository,
            MemberRepository memberRepository,
            SimplifiedRecommendationService recommendationService) {
        this.surveyRepository = surveyRepository;
        this.memberRepository = memberRepository;
        this.recommendationService = recommendationService;
    }

    /**
     * 사용자의 설문조사를 저장하고 즉시 강의 추천을 제공합니다.
     * @param memberId 회원 ID
     * @param surveyRequest 설문조사 요청 DTO
     * @return 추천된 강의 목록
     */
    public RecommendationResponse submitSurveyAndGetRecommendations(Long memberId, SurveyRequest surveyRequest) {
        log.info("설문 제출 시작 - 회원 ID: {}, 게임 타입: {}", memberId, surveyRequest.getGameType());

        // 1. 회원 정보 조회
        Member member;
        try {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));
            log.info("회원 정보 조회 완료 - 닉네임: {}", member.getNickname());
        } catch (Exception e) {
            log.error("회원 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.", e);
        }

        // 2. 설문조사 엔티티 생성 및 저장
        Survey survey = createSurveyFromRequest(member, surveyRequest);

        Survey savedSurvey;
        try {
            savedSurvey = surveyRepository.save(survey);
            log.info("설문 저장 성공 - 설문 ID: {}", savedSurvey.getId());
        } catch (Exception e) {
            log.error("설문 저장 실패: {}", e.getMessage());
            throw new RuntimeException("설문 저장 중 오류가 발생했습니다.", e);
        }

        // 3. 저장된 설문조사를 기반으로 즉시 강의 추천 실행
        try {
            RecommendationResponse recommendations = recommendationService.getRecommendations(member, savedSurvey);
            log.info("강의 추천 성공 - 추천 개수: {}",
                    recommendations.getRecommendations() != null ? recommendations.getRecommendations().size() : 0);
            return recommendations;
        } catch (Exception e) {
            log.error("강의 추천 생성 실패: {}", e.getMessage());
            throw new RuntimeException("강의 추천 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 설문 요청 DTO에서 설문 엔티티를 생성합니다.
     */
    private Survey createSurveyFromRequest(Member member, SurveyRequest request) {
        Survey survey = new Survey();
        survey.setMember(member);
        survey.setGameType(request.getGameType());
        survey.setGameTier(request.getGameTier());
        survey.setGamePosition(request.getGamePosition());
        survey.setSkillLevel(request.getSkillLevel());
        survey.setLearningGoal(request.getLearningGoal());
        survey.setAvailableTime(request.getAvailableTime());
        survey.setLecturePreference(request.getLecturePreference());
        return survey;
    }

    /**
     * 특정 회원의 모든 설문 이력을 조회합니다.
     * @param memberId 회원 ID
     * @return 회원의 설문 목록
     */
    public List<Survey> getMemberSurveys(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));
        
        return surveyRepository.findByMemberOrderByIdDesc(member);
    }

    /**
     * 설문 ID로 설문 상세 정보를 조회합니다.
     * @param surveyId 설문 ID
     * @return 설문 정보
     */
    public Survey getSurveyById(Long surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("설문을 찾을 수 없습니다: " + surveyId));
    }
}