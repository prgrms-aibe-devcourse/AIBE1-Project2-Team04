package com.reboot.survey.service;

import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.dto.SurveyRequest;
import com.reboot.survey.entity.Survey;
import com.reboot.survey.repository.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final RecommendationService recommendationService;

    @Autowired
    public SurveyService(
            SurveyRepository surveyRepository,
            MemberRepository memberRepository,
            RecommendationService recommendationService) {
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
        // 1. 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));

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

        Survey savedSurvey = surveyRepository.save(survey);

        // 3. 저장된 설문조사를 기반으로 즉시 강의 추천 실행
        RecommendationResponse recommendations = recommendationService.getRecommendations(member, savedSurvey);

        return recommendations;
    }
}
