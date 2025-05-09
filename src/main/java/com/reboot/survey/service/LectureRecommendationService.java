package com.reboot.survey.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.survey.dto.LectureRecommendation;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.entity.Survey;
import com.reboot.survey.entity.enums.GameType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LectureRecommendationService {

    private final LectureRepository lectureRepository;
    private final OkHttpClient httpClient;

    @Value("${together.ai.api.key}")
    private String togetherApiKey;

    @Value("${together.ai.api.url:https://api.together.xyz/v1/chat/completions}")
    private String togetherApiUrl;

    @Value("${together.ai.model:meta-llama/Meta-Llama-3.1-8B-Instruct-Turbo}")
    private String togetherModel;

    public LectureRecommendationService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
        this.httpClient = new OkHttpClient();
    }

    /**
     * 설문조사 결과를 바탕으로 게임 유형 및 티어로 필터링하여 강의를 추천합니다.
     * 벡터 데이터베이스 대신 직접 필터링 및 Together AI를 활용합니다.
     */
    public RecommendationResponse getRecommendations(Member member, Survey survey) {
        log.info("강의 추천 시작 - 회원 ID: {}, 설문 ID: {}", member.getMemberId(), survey.getId());

        // 1. 게임 유형과 티어로 강의 필터링
        List<Lecture> filteredLectures = filterLecturesByGameAndTier(survey.getGameType(), survey.getGameTier(), survey.getGamePosition());

        if (filteredLectures.isEmpty()) {
            return createEmptyRecommendation(survey);
        }

        // 2. Together AI에 전송할 프롬프트 생성
        String prompt = buildPromptForTogetherAI(member, survey, filteredLectures);

        // 3. Together AI API 호출
        String completion = callTogetherAI(prompt);

        // 4. API 응답 파싱하여 추천 결과 생성
        RecommendationResponse response = parseRecommendations(completion, survey.getId());

        // 5. 추천된 강의에 추가 정보(이미지 URL, 가격) 추가
        enrichRecommendations(response, filteredLectures);

        return response;
    }

    /**
     * 게임 유형과 티어에 맞는 강의를 필터링합니다.
     * 필요에 따라 포지션 필터링도 추가합니다.
     */
    private List<Lecture> filterLecturesByGameAndTier(GameType gameType, String gameTier, String gamePosition) {
        List<Lecture> allLectures = lectureRepository.findAll();

        // 활성 상태인 강의만 필터링 (삭제되지 않은 강의)
        return allLectures.stream()
                .filter(lecture -> lecture.getInfo().getGameType().equalsIgnoreCase(gameType.name()))
                .filter(lecture -> {
                    // 티어 필터링 (정확히 일치하거나, ALL이거나, 해당 티어를 포함하면 통과)
                    String lectureRank = lecture.getInfo().getLectureRank();
                    return lectureRank.equals("ALL") ||
                            lectureRank.contains(gameTier) ||
                            lectureRank.contains("ALL");
                })
                .filter(lecture -> {
                    // 포지션 필터링 (포지션이 있는 경우에만)
                    if (gamePosition == null || gamePosition.isEmpty()) {
                        return true; // 포지션이 지정되지 않았으면 모든 강의 통과
                    }

                    String lecturePosition = lecture.getInfo().getPosition();
                    return lecturePosition.equals("ALL") ||
                            lecturePosition.contains(gamePosition) ||
                            lecturePosition.contains("ALL");
                })
                .collect(Collectors.toList());
    }

    /**
     * Together AI에 전송할 프롬프트를 구성합니다.
     */
    private String buildPromptForTogetherAI(Member member, Survey survey, List<Lecture> lectures) {
        StringBuilder promptBuilder = new StringBuilder();

        // 시스템 메시지 부분
        promptBuilder.append("당신은 게임 강의 추천 시스템입니다. 회원의 설문 데이터와 강의 정보를 분석하여 가장 적합한 3개의 강의를 추천해야 합니다.\n\n");

        // 회원 정보
        promptBuilder.append("## 회원 정보\n");
        promptBuilder.append("이름: ").append(member.getName()).append("\n");
        promptBuilder.append("닉네임: ").append(member.getNickname()).append("\n\n");

        // 설문 데이터
        promptBuilder.append("## 설문 데이터\n");
        promptBuilder.append("게임 타입: ").append(survey.getGameType().name()).append("\n");
        promptBuilder.append("게임 티어: ").append(survey.getGameTier()).append("\n");
        promptBuilder.append("게임 포지션: ").append(survey.getGamePosition()).append("\n");
        promptBuilder.append("실력 수준: ").append(survey.getSkillLevel().name()).append("\n");
        promptBuilder.append("학습 목표: ").append(survey.getLearningGoal().name()).append("\n");
        promptBuilder.append("가능 시간: ").append(survey.getAvailableTime().name()).append("\n");
        promptBuilder.append("강의 선호도: ").append(survey.getLecturePreference().name()).append("\n\n");

        // 필터링된 강의 목록
        promptBuilder.append("## 필터링된 강의 목록\n");

        for (int i = 0; i < lectures.size(); i++) {
            Lecture lecture = lectures.get(i);
            Instructor instructor = lecture.getInstructor();

            promptBuilder.append(i + 1).append(". 강의 ID: ").append(lecture.getId()).append("\n");
            promptBuilder.append("   제목: ").append(lecture.getInfo().getTitle()).append("\n");
            promptBuilder.append("   설명: ").append(lecture.getInfo().getDescription()).append("\n");
            promptBuilder.append("   게임 타입: ").append(lecture.getInfo().getGameType()).append("\n");
            promptBuilder.append("   랭크/티어: ").append(lecture.getInfo().getLectureRank()).append("\n");
            promptBuilder.append("   포지션: ").append(lecture.getInfo().getPosition()).append("\n");
            promptBuilder.append("   가격: ").append(lecture.getInfo().getPrice()).append("\n");
            promptBuilder.append("   강의 시간: ").append(lecture.getInfo().getDuration()).append("분\n");
            promptBuilder.append("   평균 평점: ").append(lecture.getMetadata().getAverageRating()).append("\n");
            promptBuilder.append("   수강생 수: ").append(lecture.getMetadata().getTotalMembers()).append("\n");
            promptBuilder.append("   리뷰 수: ").append(lecture.getMetadata().getReviewCount()).append("\n");
            promptBuilder.append("   강사 이름: ").append(instructor.getMember().getName()).append("\n");
            promptBuilder.append("   강사 닉네임: ").append(instructor.getMember().getNickname()).append("\n");
            promptBuilder.append("   강사 경력: ").append(instructor.getCareer()).append("\n");
            promptBuilder.append("   강사 설명: ").append(instructor.getDescription()).append("\n");
            promptBuilder.append("   강사 평점: ").append(instructor.getAverageRating()).append("\n\n");
        }

        // 요청 사항
        promptBuilder.append("## 요청 사항\n");
        promptBuilder.append("위 회원의 설문 정보와 제공된 강의 목록을 분석하여 가장 적합한 3개의 강의를 추천해주세요.\n");
        promptBuilder.append("각 추천에는 다음 내용을 포함해야 합니다:\n");
        promptBuilder.append("1. 강의 ID와 제목\n");
        promptBuilder.append("2. 강사 이름과 경력\n");
        promptBuilder.append("3. 해당 강의가 이 회원에게 적합한 이유\n");
        promptBuilder.append("4. 이 강의를 통해 기대할 수 있는 효과\n\n");

        // 응답 형식
        promptBuilder.append("## 응답 형식\n");
        promptBuilder.append("""
        {member_nickname}님을 위한 맞춤 강의 추천

        1. [강의 ID: {lecture_id}] {lecture_title}
           강사: {instructor_name} ({instructor_career})
           추천 이유: {detailed_reason_based_on_survey}
           기대 효과: {expected_improvement}

        2. [강의 ID: {lecture_id}] {lecture_title}
           강사: {instructor_name} ({instructor_career})
           추천 이유: {detailed_reason_based_on_survey}
           기대 효과: {expected_improvement}

        3. [강의 ID: {lecture_id}] {lecture_title}
           강사: {instructor_name} ({instructor_career})
           추천 이유: {detailed_reason_based_on_survey}
           기대 효과: {expected_improvement}

        종합 추천 의견: {overall_recommendation}
        """);

        return promptBuilder.toString();
    }

    /**
     * Together AI API를 호출하여 추천 결과를 얻습니다.
     */
    private String callTogetherAI(String prompt) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", togetherModel);

        JSONArray messages = new JSONArray();

        // 시스템 메시지
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 게임 강의 추천 전문가입니다. 설문 결과와 강의 목록을 분석하여 최적의 강의를 추천해주세요.");

        // 유저 메시지 (프롬프트)
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        messages.put(systemMessage);
        messages.put(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 1000);

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(togetherApiUrl)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + togetherApiKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Together AI API 호출 실패: {}", response);
                return createFallbackRecommendationText();
            }

            if (response.body() == null) {
                log.error("Together AI API 응답 body가 null입니다.");
                return createFallbackRecommendationText();
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Together AI 응답에서 생성된 내용 추출
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content");
            }

            return createFallbackRecommendationText();
        } catch (IOException e) {
            log.error("Together AI API 호출 중 오류 발생", e);
            return createFallbackRecommendationText();
        }
    }

    /**
     * API 호출 실패 시 기본 추천 텍스트를 생성합니다.
     */
    private String createFallbackRecommendationText() {
        return """
               회원님을 위한 맞춤 강의 추천

               1. [강의 ID: 1] 게임 기본기 마스터하기
                  강사: 김코치 (전문 게임 코치 5년 경력)
                  추천 이유: 회원님의 학습 목표와 실력 수준에 적합한 기초 강의입니다.
                  기대 효과: 게임에 대한 기본적인 이해와 실력 향상

               2. [강의 ID: 2] 포지션별 전략 심화
                  강사: 이코치 (프로게이머 출신 코치)
                  추천 이유: 회원님의 포지션에 특화된 전략을 배울 수 있습니다.
                  기대 효과: 특정 포지션에서의 플레이 향상

               3. [강의 ID: 3] 티어 상승을 위한 핵심 스킬
                  강사: 박코치 (고수 강사)
                  추천 이유: 현재 티어에서 한 단계 상승하기 위한 필수 스킬을 배웁니다.
                  기대 효과: 랭크 게임에서의 승률 향상

               종합 추천 의견: 회원님의 현재 실력 수준과 목표를 고려하여 단계적으로 학습할 수 있는 강의를 추천드립니다.
               """;
    }

    /**
     * 빈 강의 추천 응답을 생성합니다.
     */
    private RecommendationResponse createEmptyRecommendation(Survey survey) {
        RecommendationResponse response = new RecommendationResponse();
        response.setSurveyId(survey.getId());
        response.setOverallRecommendation("현재 귀하의 기준에 맞는 강의가 준비되어 있지 않습니다. 곧 더 많은 강의가 추가될 예정입니다.");
        response.setRecommendations(new ArrayList<>());
        return response;
    }

    /**
     * Together AI의 응답을 파싱하여 RecommendationResponse 객체로 변환합니다.
     */
    private RecommendationResponse parseRecommendations(String aiResponseText, Long surveyId) {
        RecommendationResponse response = new RecommendationResponse();
        response.setSurveyId(surveyId);
        List<LectureRecommendation> recommendations = new ArrayList<>();

        // 강의 추천 정보 추출을 위한 정규식 패턴
        Pattern lecturePattern = Pattern.compile(
                "\\[강의 ID: (\\d+)\\] ([^\n]+)\\s*\n\\s*강사: ([^(]+)\\(([^)]+)\\)\\s*\n\\s*추천 이유: ([^\n]+)\\s*\n\\s*기대 효과: ([^\n]+)",
                Pattern.DOTALL
        );

        Matcher lectureMatcher = lecturePattern.matcher(aiResponseText);

        // 강의 추천 찾기
        while (lectureMatcher.find() && recommendations.size() < 3) {
            LectureRecommendation recommendation = new LectureRecommendation();

            try {
                recommendation.setLectureId(Long.parseLong(lectureMatcher.group(1).trim()));
                recommendation.setTitle(lectureMatcher.group(2).trim());
                recommendation.setInstructorName(lectureMatcher.group(3).trim());
                recommendation.setInstructorCareer(lectureMatcher.group(4).trim());
                recommendation.setRecommendationReason(lectureMatcher.group(5).trim());
                recommendation.setExpectedEffect(lectureMatcher.group(6).trim());

                recommendations.add(recommendation);
            } catch (Exception e) {
                log.error("강의 추천 정보 파싱 중 오류 발생: {}", e.getMessage());
                // 파싱 오류 시 해당 항목은 건너뜀
            }
        }

        // 종합 추천 의견 추출
        Pattern overallPattern = Pattern.compile("종합 추천 의견: ([^\n]+)");
        Matcher overallMatcher = overallPattern.matcher(aiResponseText);

        if (overallMatcher.find()) {
            response.setOverallRecommendation(overallMatcher.group(1).trim());
        } else {
            response.setOverallRecommendation("회원님의 설문 결과를 바탕으로 맞춤형 강의를 추천해 드립니다.");
        }

        response.setRecommendations(recommendations);
        return response;
    }

    /**
     * 추천 강의에 추가 정보(이미지 URL, 가격)를 보강합니다.
     */
    private void enrichRecommendations(RecommendationResponse response, List<Lecture> allLectures) {
        if (response.getRecommendations() == null || response.getRecommendations().isEmpty()) {
            return;
        }

        for (LectureRecommendation recommendation : response.getRecommendations()) {
            // lectureId로 해당 강의 찾기
            allLectures.stream()
                    .filter(lecture -> lecture.getId().equals(recommendation.getLectureId()))
                    .findFirst()
                    .ifPresent(lecture -> {
                        // 이미지 URL 및 가격 정보 추가
                        recommendation.setImageUrl(lecture.getInfo().getImageUrl());
                        recommendation.setPrice(lecture.getInfo().getPrice());
                    });
        }
    }
}