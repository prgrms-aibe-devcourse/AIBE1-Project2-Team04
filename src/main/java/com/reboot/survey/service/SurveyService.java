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

@Service
@Transactional
public class SurveyService {

    private final Logger log = LoggerFactory.getLogger(SurveyService.class);
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final LectureRecommendationService lectureRecommendationService; // 변경된 서비스 주입

    @Autowired
    public SurveyService(
            SurveyRepository surveyRepository,
            MemberRepository memberRepository,
            LectureRecommendationService lectureRecommendationService) { // 변경된 서비스 주입
        this.surveyRepository = surveyRepository;
        this.memberRepository = memberRepository;
        this.lectureRecommendationService = lectureRecommendationService;
    }

    /**
     * 사용자의 설문조사를 저장하고 즉시 강의 추천을 제공합니다.
     * @param memberId 회원 ID
     * @param surveyRequest 설문조사 요청 DTO
     * @return 추천된 강의 목록
     */
    public RecommendationResponse submitSurveyAndGetRecommendations(Long memberId, SurveyRequest surveyRequest) {
        log.info("설문 제출 시작 - 회원 ID: {}", memberId);

        // 1. 회원 정보 조회
        Member member;
        try {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));
        } catch (Exception e) {
            log.error("회원 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.", e);
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
            savedSurvey = surveyRepository.save(survey);
            log.info("설문 저장 성공 - 설문 ID: {}", savedSurvey.getId());
        } catch (Exception e) {
            log.error("설문 저장 실패: {}", e.getMessage());
            throw new RuntimeException("설문 저장 중 오류가 발생했습니다.", e);
        }

        // 3. 저장된 설문조사를 기반으로 즉시 강의 추천 실행 (새로운 서비스 호출)
        try {
            RecommendationResponse recommendations = lectureRecommendationService.getRecommendations(member, savedSurvey);
            log.info("강의 추천 성공 - 추천 개수: {}",
                    recommendations.getRecommendations() != null ? recommendations.getRecommendations().size() : 0);
            return recommendations;
        } catch (Exception e) {
            log.error("강의 추천 생성 실패: {}", e.getMessage());
            throw new RuntimeException("강의 추천 생성 중 오류가 발생했습니다.", e);
        }
    }
}