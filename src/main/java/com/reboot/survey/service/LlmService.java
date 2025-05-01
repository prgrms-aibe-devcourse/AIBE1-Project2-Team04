package com.reboot.survey.service;

import com.reboot.auth.entity.Member;
import com.reboot.survey.entity.Survey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import java.util.List;

@Service
public class LlmService {

    private final String SYSTEM_PROMPT = "당신은 게임 강의 추천 시스템입니다. 회원의 설문 데이터와 시스템에 있는 강의 정보를 분석하여 가장 적합한 5개의 강의를 추천해야 합니다.\n\n" +
            "각 추천에는 다음이 포함되어야 합니다:\n" +
            "1. 강의 이름과 ID\n" +
            "2. 강사 이름과 간략한 설명\n" +
            "3. 해당 강의가 이 회원에게 적합한 이유 (설문 데이터와의 연관성)\n" +
            "4. 회원의 게임 실력 향상을 위한 제안\n\n" +
            "프로세스:\n" +
            "1. 회원의 설문 데이터(게임 타입, 티어, 포지션, 학습 목표, 가능 시간, 강의 선호도)를 분석하세요.\n" +
            "2. 검색된 강의 데이터를 검토하세요.\n" +
            "3. 회원의 요구와 가장 잘 맞는 강의를 식별하세요.\n" +
            "4. 각 강의가 이 회원에게 어떻게 도움이 될 수 있는지 구체적으로 설명하세요.\n\n" +
            "강의 추천 결과는 회원이 바로 이해할 수 있도록 명확하고 친근한 톤으로 작성하세요.";

    private final String RESPONSE_FORMAT = "추천 형식:\n" +
            "{member_nickname}님을 위한 맞춤 강의 추천\n\n" +
            "1. [강의 ID: {lecture_id}] {lecture_title}\n" +
            "   강사: {instructor_name} ({instructor_career})\n" +
            "   추천 이유: {detailed_reason_based_on_survey}\n" +
            "   기대 효과: {expected_improvement}\n\n" +
            "2. [강의 ID: {lecture_id}] {lecture_title}\n" +
            "   ...\n\n" +
            "종합 추천 의견: {overall_recommendation}";

    private final LlmClient llmClient;

    @Autowired
    public LlmService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 회원 정보, 설문 데이터, 검색된 강의 정보를 기반으로 LLM에 추천을 요청합니다.
     */
    public String generateRecommendations(Member member, Survey survey, List<Document> retrievedLectures) {
        // 프롬프트 구성
        String prompt = buildPrompt(member, survey, retrievedLectures);

        // LLM API 호출
        LlmRequest request = new LlmRequest();
        request.setSystemPrompt(SYSTEM_PROMPT);
        request.setUserPrompt(prompt);
        request.setResponseFormat(RESPONSE_FORMAT);
        request.setTemperature(0.3f); // 일관된 결과를 위해 낮은 온도 설정
        request.setMaxTokens(2000);

        LlmResponse response = llmClient.generateCompletion(request);

        return response.getCompletion();
    }

    /**
     * 회원 정보, 설문 데이터, 검색된 강의 정보를 기반으로 프롬프트를 구성합니다.
     */
    private String buildPrompt(Member member, Survey survey, List<Document> retrievedLectures) {
        // 회원 정보 포맷팅
        String memberInfo = String.format(
                "ID: %d\n이름: %s\n닉네임: %s",
                member.getMemberId(),
                member.getName(),
                member.getNickname()
        );

        // 설문 데이터 포맷팅
        String surveyData = String.format(
                "게임 타입: %s\n게임 티어: %s\n게임 포지션: %s\n실력 수준: %s\n학습 목표: %s\n가능한 시간: %s\n강의 선호도: %s",
                survey.getGameType().name(),
                survey.getGameTier(),
                survey.getGamePosition(),
                survey.getSkillLevel().name(),
                survey.getLearningGoal().name(),
                survey.getAvailableTime().name(),
                survey.getLecturePreference().name()
        );

        // 검색된 강의 정보 포맷팅
        StringBuilder lecturesData = new StringBuilder();
        for (int i = 0; i < retrievedLectures.size(); i++) {
            Document doc = retrievedLectures.get(i);
            lecturesData.append(i + 1).append(". ").append(doc.getContent()).append("\n\n");
        }

        // 최종 프롬프트 구성
        return String.format(
                "다음은 회원 정보와 설문 데이터입니다:\n\n[회원 정보]\n%s\n\n[설문 데이터]\n%s\n\n다음은 벡터 검색으로 찾은 관련성 높은 강의 목록입니다:\n%s\n\n이 회원에게 가장 적합한 5개의 강의를 추천하고, 각각에 대해 추천 이유를 설명해주세요.",
                memberInfo,
                surveyData,
                lecturesData.toString()
        );
    }
}

