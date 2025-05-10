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
import java.util.Arrays;
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
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * 설문조사 결과를 바탕으로 게임 유형 및 티어로 필터링하여 강의를 추천합니다.
     * LLM 체이닝 방식으로 첫 번째 프롬프트에서 강의 설명을 요약하고, 두 번째 프롬프트에서 추천을 생성합니다.
     */
    public RecommendationResponse getRecommendations(Member member, Survey survey) {
        log.info("강의 추천 시작 - 회원 ID: {}, 설문 ID: {}", member.getMemberId(), survey.getId());

        // 회원 게임 정보 로그
        if (member.getGame() != null) {
            log.info("회원 게임 정보 - 타입: {}, 티어: {}, 포지션: {}",
                    member.getGame().getGameType(),
                    member.getGame().getGameTier(),
                    member.getGame().getGamePosition());
        }

        // 1. 게임 유형과 티어로 강의 필터링 (개선된 필터링 로직 적용)
        List<Lecture> filteredLectures = filterLecturesByGameAndTier(
                survey.getGameType(),
                survey.getGameTier(),
                survey.getGamePosition()
        );
        log.info("필터링된 강의 수: {}", filteredLectures.size());

        if (filteredLectures.isEmpty()) {
            log.warn("필터링된 강의가 없습니다. 빈 추천 결과를 반환합니다.");
            return createEmptyRecommendation(survey);
        }

        // 2. 첫 번째 LLM 호출: 각 강의 설명을 요약하기
        List<String> summarizedLectures = summarizeLectureDescriptions(filteredLectures);
        log.info("요약된 강의 설명 수: {}", summarizedLectures.size());

        // 3. 두 번째 LLM 호출: 요약된 강의를 이용하여 추천 생성하기
        String recommendationPrompt = buildRecommendationPrompt(member, survey, filteredLectures, summarizedLectures);
        String completion = callTogetherAIWithImprovedParams(recommendationPrompt);

        // 4. API 응답 파싱하여 추천 결과 생성 (개선된 정규식 패턴 적용)
        RecommendationResponse response = parseRecommendationsWithImprovedPattern(completion, survey.getId());
        log.info("파싱된 추천 결과 - 강의 수: {}",
                response.getRecommendations() != null ? response.getRecommendations().size() : 0);

        // 5. 추천된 강의에 추가 정보(이미지 URL, 가격) 추가
        enrichRecommendations(response, filteredLectures);

        // 6. 응답 결과 정제 (새로 추가된 기능)
        response.setRecommendations(refineRecommendations(response.getRecommendations()));

        return response;
    }

    /**
     * 각 강의 설명을 요약합니다. (첫 번째 LLM 호출)
     */
    private List<String> summarizeLectureDescriptions(List<Lecture> lectures) {
        List<String> summarizedLectures = new ArrayList<>();

        log.info("강의 설명 요약 시작: {} 개의 강의", lectures.size());
        int processedCount = 0;

        for (Lecture lecture : lectures) {
            String summarizationPrompt = buildSummarizationPrompt(lecture);
            String summarizedDescription = callTogetherAIForSummarization(summarizationPrompt);
            processedCount++;

            if (summarizedDescription != null && !summarizedDescription.isEmpty()) {
                summarizedLectures.add(summarizedDescription);
                log.info("강의 요약 완료 ({}/{}): 강의 ID: {}", processedCount, lectures.size(), lecture.getId());
            } else {
                log.warn("강의 요약 실패 ({}/{}): 강의 ID: {}", processedCount, lectures.size(), lecture.getId());
                // 요약 실패 시 원본 데이터의 일부 사용
                String fallbackSummary = buildFallbackSummary(lecture);
                summarizedLectures.add(fallbackSummary);
            }
        }

        log.info("강의 설명 요약 완료: 총 {} 개 요약됨", summarizedLectures.size());
        return summarizedLectures;
    }

    /**
     * 요약 실패 시 기본 요약본을 생성합니다.
     */
    private String buildFallbackSummary(Lecture lecture) {
        StringBuilder summary = new StringBuilder();

        // 기본 정보
        summary.append("강의 ID: ").append(lecture.getId()).append("\n");
        summary.append("제목: ").append(lecture.getInfo().getTitle()).append("\n");

        // 설명이 있으면 앞부분만 추출
        if (lecture.getInfo().getDescription() != null && !lecture.getInfo().getDescription().isEmpty()) {
            String desc = lecture.getInfo().getDescription();
            summary.append("요약: ").append(desc.length() > 150 ? desc.substring(0, 150) + "..." : desc).append("\n");
        } else {
            summary.append("요약: 정보 없음\n");
        }

        // 게임 정보
        summary.append("게임 타입: ").append(lecture.getInfo().getGameType()).append("\n");
        summary.append("랭크/티어: ").append(lecture.getInfo().getLectureRank()).append("\n");
        summary.append("포지션: ").append(lecture.getInfo().getPosition()).append("\n");

        // 강의 세부 정보
        summary.append("가격: ").append(lecture.getInfo().getPrice()).append("\n");
        summary.append("강의 시간: ").append(lecture.getInfo().getDuration()).append("분\n");

        // 통계 정보
        summary.append("평균 평점: ").append(lecture.getMetadata().getAverageRating()).append("\n");
        summary.append("수강생 수: ").append(lecture.getMetadata().getTotalMembers()).append("\n");
        summary.append("리뷰 수: ").append(lecture.getMetadata().getReviewCount()).append("\n");

        // 강사 정보
        Instructor instructor = lecture.getInstructor();
        if (instructor != null && instructor.getMember() != null) {
            summary.append("강사: ").append(instructor.getMember().getName());
            if (instructor.getCareer() != null && !instructor.getCareer().isEmpty()) {
                summary.append(" (").append(instructor.getCareer()).append(")");
            }
            summary.append("\n");
        } else {
            summary.append("강사: 정보 없음\n");
        }

        return summary.toString();
    }

    /**
     * 강의 설명 요약을 위한 프롬프트를 구성합니다.
     */
    private String buildSummarizationPrompt(Lecture lecture) {
        StringBuilder promptBuilder = new StringBuilder();
        Instructor instructor = lecture.getInstructor();

        // 시스템 메시지
        promptBuilder.append("당신은 게임 강의 요약 전문가입니다. 주어진 강의에 대한 정보를 500자 이내로 간결하게 요약해주세요.\n\n");

        // 강의 정보 - 상세 형식으로
        promptBuilder.append("## 강의 정보\n");
        promptBuilder.append("강의 ID: ").append(lecture.getId()).append("\n");
        promptBuilder.append("제목: ").append(lecture.getInfo().getTitle()).append("\n");
        promptBuilder.append("설명: ").append(lecture.getInfo().getDescription()).append("\n");

        // 게임 정보
        promptBuilder.append("게임 타입: ").append(lecture.getInfo().getGameType()).append("\n");
        promptBuilder.append("랭크/티어: ").append(lecture.getInfo().getLectureRank()).append("\n");
        promptBuilder.append("포지션: ").append(lecture.getInfo().getPosition()).append("\n");

        // 강의 상세 정보
        promptBuilder.append("가격: ").append(lecture.getInfo().getPrice()).append("\n");
        promptBuilder.append("강의 시간: ").append(lecture.getInfo().getDuration()).append("분\n");

        // 통계 정보
        promptBuilder.append("평균 평점: ").append(lecture.getMetadata().getAverageRating()).append("\n");
        promptBuilder.append("수강생 수: ").append(lecture.getMetadata().getTotalMembers()).append("\n");
        promptBuilder.append("리뷰 수: ").append(lecture.getMetadata().getReviewCount()).append("\n");

        // 강사 정보
        if (instructor != null && instructor.getMember() != null) {
            promptBuilder.append("강사 이름: ").append(instructor.getMember().getName()).append("\n");
            promptBuilder.append("강사 닉네임: ").append(instructor.getMember().getNickname()).append("\n");
            promptBuilder.append("강사 경력: ").append(instructor.getCareer() != null ? instructor.getCareer() : "정보 없음").append("\n");
            promptBuilder.append("강사 설명: ").append(instructor.getDescription() != null ? instructor.getDescription() : "정보 없음").append("\n");
            promptBuilder.append("강사 평점: ").append(instructor.getAverageRating()).append("\n\n");
        } else {
            promptBuilder.append("강사 정보: 정보 없음\n\n");
        }

        // 요청사항 - 명확한 형식 지정
        promptBuilder.append("## 요청 사항\n");
        promptBuilder.append("위 강의 정보를 500자 이내로 요약해주세요. 다음 형식을 반드시 지켜주세요:\n\n");
        promptBuilder.append("강의 ID: {lecture_id}\n");
        promptBuilder.append("제목: {lecture_title}\n");
        promptBuilder.append("요약: {concise_description_within_500_chars}\n");
        promptBuilder.append("게임 타입: {game_type}\n");
        promptBuilder.append("랭크/티어: {lecture_rank}\n");
        promptBuilder.append("포지션: {position}\n");
        promptBuilder.append("강의 유형: {lecture_type}\n");
        promptBuilder.append("가격: {price}\n");
        promptBuilder.append("강의 시간: {duration}분\n");
        promptBuilder.append("강사: {instructor_name} ({instructor_career})\n");
        promptBuilder.append("평균 평점: {average_rating}\n");

        return promptBuilder.toString();
    }

    /**
     * 강의 설명 요약을 위한 Together AI API 호출
     */
    private String callTogetherAIForSummarization(String prompt) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", togetherModel);

        JSONArray messages = new JSONArray();

        // 시스템 메시지
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 게임 강의 요약 전문가입니다. 강의 정보를 간결하게 요약해주세요.");

        // 유저 메시지 (프롬프트)
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        messages.put(systemMessage);
        messages.put(userMessage);

        requestBody.put("messages", messages);

        // 요약은 간결하게 하도록 파라미터 조정
        requestBody.put("temperature", 0.2); // 낮은 온도로 일관성 있는 출력
        requestBody.put("max_tokens", 500); // 요약이므로 토큰 제한
        requestBody.put("top_p", 0.8);

        log.info("요약을 위한 Together AI API 호출 시작");
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
                log.error("요약 API 호출 실패: 코드={}, 메시지={}",
                        response.code(), response.message());
                return null;
            }

            if (response.body() == null) {
                log.error("요약 API 응답 body가 null입니다.");
                return null;
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

            log.warn("요약 API 응답에 choices가 없습니다.");
            return null;
        } catch (IOException e) {
            log.error("요약 API 호출 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 추천을 위한 두 번째 프롬프트를 구성합니다.
     */
    private String buildRecommendationPrompt(Member member, Survey survey, List<Lecture> lectures, List<String> summarizedLectures) {
        StringBuilder promptBuilder = new StringBuilder();

        // 시스템 메시지 부분 - 명확한 지시사항 추가
        promptBuilder.append("당신은 게임 강의 추천 전문가입니다. 회원의 설문 데이터와 강의 정보를 깊이 분석하여 가장 적합한 3개의 강의를 추천해야 합니다.\n\n");

        // 설문 분석 파트 추가 - 더 구조화된 형태 및 모든 설문 항목 반영
        promptBuilder.append("## 설문 분석\n");
        promptBuilder.append("이 사용자는 ").append(survey.getGameType()).append(" 게임의 ");
        promptBuilder.append(survey.getSkillLevel()).append(" 수준의 플레이어로, ");

        if (survey.getGamePosition() != null && !survey.getGamePosition().isEmpty()) {
            promptBuilder.append(survey.getGamePosition()).append(" 포지션을 선호합니다. ");
        }

        promptBuilder.append("학습 목표는 ").append(survey.getLearningGoal()).append("이며, ");
        promptBuilder.append("주당 ").append(survey.getAvailableTime()).append(" 정도의 시간을 투자할 수 있습니다. ");

        promptBuilder.append("강의 선호도는 ").append(survey.getLecturePreference()).append("입니다. ");

        promptBuilder.append("\n\n");

        // 회원 정보 - 기존과 동일하게 유지
        promptBuilder.append("## 회원 정보\n");
        promptBuilder.append("이름: ").append(member.getName()).append("\n");
        promptBuilder.append("닉네임: ").append(member.getNickname()).append("\n");

        // 회원 게임 정보가 있으면 추가
        if (member.getGame() != null) {
            promptBuilder.append("게임 타입: ").append(member.getGame().getGameType()).append("\n");
            promptBuilder.append("게임 티어: ").append(member.getGame().getGameTier()).append("\n");
            promptBuilder.append("게임 포지션: ").append(member.getGame().getGamePosition()).append("\n");
        }
        promptBuilder.append("\n");

        // 요약된 강의 목록 추가
        promptBuilder.append("## 요약된 강의 목록\n");
        for (int i = 0; i < summarizedLectures.size(); i++) {
            promptBuilder.append(i + 1).append(". ").append(summarizedLectures.get(i)).append("\n\n");
        }

        // LLM 지시사항 명확화 - 더 구체적인 지시 추가
        promptBuilder.append("## 요청 사항\n");
        promptBuilder.append("위 회원의 설문 정보와 제공된 강의 목록을 깊이 분석하여 다음 기준에 따라 가장 적합한 3개의 강의를 추천해주세요:\n");
        promptBuilder.append("1. 게임 타입 일치도 (필수): 추천 강의는 반드시 회원이 선택한 게임 타입과 일치해야 합니다.\n");
        promptBuilder.append("2. 실력 수준 적합도: 회원의 현재 실력과 티어에 적합한 강의여야 합니다.\n");
        promptBuilder.append("3. 포지션 일치도: 가능한 회원이 선호하는 포지션과 일치하는 강의를 추천하세요.\n");
        promptBuilder.append("4. 학습 목표 부합도: 회원의 학습 목표를 달성하는데 도움이 되는 강의를 추천하세요.\n");
        promptBuilder.append("5. 시간 투자 가능성: 회원이 투자할 수 있는 시간을 고려하여 적절한 강의를 추천하세요.\n");
        promptBuilder.append("6. 강의 선호도 반영: 회원이 선호하는 강의 유형(1:1, 그룹, 녹화 등)을 고려하세요.\n");
        promptBuilder.append("7. 추천 이유와 효과: 각 강의가 왜 이 회원에게 적합한지, 어떤 효과를 기대할 수 있는지 상세히 설명하세요.\n\n");

        // 응답 형식 - 기존과 동일하게 유지
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
     * 게임 유형과 티어에 맞는 강의를 필터링합니다.
     * 필요에 따라 포지션 필터링도 추가합니다.
     * 티어 호환성 로직 추가
     */
    private List<Lecture> filterLecturesByGameAndTier(GameType gameType, String gameTier, String gamePosition) {
        // 로그 추가
        log.info("강의 필터링 시작 - 게임: {}, 티어: {}, 포지션: {}", gameType, gameTier, gamePosition);

        List<Lecture> allLectures = lectureRepository.findAll();
        log.info("전체 강의 수: {}", allLectures.size());

        // 필터링 로직 개선
        List<Lecture> filteredLectures = allLectures.stream()
                .filter(lecture -> {
                    // null 체크
                    if (lecture.getInfo() == null) {
                        log.warn("강의 ID {}의 Info가 null입니다", lecture.getId());
                        return false;
                    }

                    // 게임 타입 필터링 (필수)
                    String lectureGameType = lecture.getInfo().getGameType();
                    if (lectureGameType == null || !lectureGameType.equalsIgnoreCase(gameType.name())) {
                        return false;
                    }

                    // 포지션 필터링 (선택적)
                    if (gamePosition != null && !gamePosition.isEmpty()) {
                        String lecturePosition = lecture.getInfo().getPosition();
                        if (lecturePosition == null ||
                                (!lecturePosition.equals("ALL") &&
                                        !lecturePosition.contains(gamePosition) &&
                                        !lecturePosition.contains("ALL"))) {
                            return false;
                        }
                    }

                    // 티어 필터링 (선택적)
                    if (gameTier != null && !gameTier.isEmpty()) {
                        String lectureRank = lecture.getInfo().getLectureRank();
                        if (lectureRank == null) {
                            return false;
                        }

                        if (!lectureRank.equals("ALL") &&
                                !lectureRank.contains(gameTier) &&
                                !lectureRank.contains("ALL")) {
                            // 티어 호환성 검사 추가
                            if (!isTierCompatible(gameTier, lectureRank)) {
                                return false;
                            }
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        log.info("필터링 후 강의 수: {}", filteredLectures.size());

        // 필터링된 강의 목록 로깅
        if (!filteredLectures.isEmpty()) {
            log.info("필터링된 강의 목록:");
            for (Lecture lecture : filteredLectures) {
                log.info(" - ID: {}, 제목: {}, 게임: {}, 티어: {}, 포지션: {}",
                        lecture.getId(),
                        lecture.getInfo().getTitle(),
                        lecture.getInfo().getGameType(),
                        lecture.getInfo().getLectureRank(),
                        lecture.getInfo().getPosition());
            }
        } else {
            log.warn("해당 조건의 강의가 없습니다");
        }

        return filteredLectures;
    }

    /**
     * 티어 호환성 검사 (낮은 티어 강의도 포함)
     */
    private boolean isTierCompatible(String userTier, String lectureTiers) {
        List<String> tierHierarchy = Arrays.asList(
                "IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"
        );

        int userTierIndex = tierHierarchy.indexOf(userTier);
        if (userTierIndex == -1) return true; // 티어 정보가 없으면 모든 강의 통과

        String[] lectureRanks = lectureTiers.split(",");
        for (String rank : lectureRanks) {
            rank = rank.trim();
            int lectureRankIndex = tierHierarchy.indexOf(rank);
            // 유저 티어가 강의 티어보다 높거나 같으면 호환 가능
            // 또는 유저 티어보다 1단계 높은 티어의 강의도 추천 가능 (-1은 허용 범위)
            if (lectureRankIndex != -1 && userTierIndex >= lectureRankIndex - 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * 개선된 파라미터로 Together AI API를 호출합니다.
     */
    private String callTogetherAIWithImprovedParams(String prompt) {
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

        // 추천 결과 다양화를 위한 파라미터 조정
        requestBody.put("temperature", 0.7); // 온도 높여서 다양성 증가
        requestBody.put("max_tokens", 2000); // 토큰 수 증가
        requestBody.put("top_p", 0.9); // 상위 확률 샘플링
        requestBody.put("presence_penalty", 0.6); // 반복 감소
        requestBody.put("frequency_penalty", 0.6); // 반복 감소

        log.info("Together AI API 호출 시작");
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
                log.error("Together AI API 호출 실패: 코드={}, 메시지={}",
                        response.code(), response.message());
                return createFallbackRecommendationText();
            }

            if (response.body() == null) {
                log.error("Together AI API 응답 body가 null입니다.");
                return createFallbackRecommendationText();
            }

            String responseBody = response.body().string();
            log.info("Together AI API 호출 성공: 응답 길이={}", responseBody.length());

            JSONObject jsonResponse = new JSONObject(responseBody);

            // Together AI 응답에서 생성된 내용 추출
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content");
            }

            log.warn("Together AI API 응답에 choices가 없습니다.");
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
     * 개선된 정규식 패턴으로 Together AI의 응답을 파싱합니다.
     */
    private RecommendationResponse parseRecommendationsWithImprovedPattern(String aiResponseText, Long surveyId) {
        RecommendationResponse response = new RecommendationResponse();
        response.setSurveyId(surveyId);
        List<LectureRecommendation> recommendations = new ArrayList<>();

        if (aiResponseText == null || aiResponseText.isEmpty()) {
            log.error("API 응답이 비어있습니다.");
            response.setOverallRecommendation("죄송합니다. 추천 생성 중 오류가 발생했습니다.");
            response.setRecommendations(recommendations);
            return response;
        }

        // 기본 정규식 패턴 (정확한 형식 매칭)
        Pattern lecturePattern = Pattern.compile(
                "\\[강의 ID: (\\d+)\\]\\s*([^\\n]+)\\s*\n\\s*강사:\\s*([^(]+)\\s*\\(([^)]+)\\)\\s*\n\\s*추천 이유:\\s*([\\s\\S]*?)\\s*\n\\s*기대 효과:\\s*([\\s\\S]*?)\\s*(?:\n|$)",
                Pattern.DOTALL
        );

        // 대체 정규식 패턴 (다양한 형식 처리)
        Pattern altPattern = Pattern.compile(
                "(\\d+)[.:\\)]\\s*\\[?(?:강의 ID:)?\\s*(\\d+)\\]?\\s*([^\\n]+)\\s*\n[\\s\\S]*?강사:\\s*([^\\n(]+)(?:\\(([^)]+)\\))?[\\s\\S]*?추천(?:\\s*이유)?:\\s*([\\s\\S]*?)(?:기대(?:\\s*효과)?:|\\d+[.:\\)])",
                Pattern.DOTALL
        );

        // 우선 기본 패턴으로 시도
        Matcher lectureMatcher = lecturePattern.matcher(aiResponseText);

        int count = 0;
        boolean foundMatches = false;

        // 기본 패턴으로 매칭 시도
        while (lectureMatcher.find() && count < 3) {
            foundMatches = true;
            LectureRecommendation recommendation = new LectureRecommendation();

            try {
                recommendation.setLectureId(Long.parseLong(lectureMatcher.group(1).trim()));
                recommendation.setTitle(lectureMatcher.group(2).trim());
                recommendation.setInstructorName(lectureMatcher.group(3).trim());
                recommendation.setInstructorCareer(lectureMatcher.group(4).trim());
                recommendation.setRecommendationReason(lectureMatcher.group(5).trim());
                recommendation.setExpectedEffect(lectureMatcher.group(6).trim());

                recommendations.add(recommendation);
                count++;
                log.info("추천 강의 #{} 파싱 성공: ID={}, 제목={}", count, recommendation.getLectureId(), recommendation.getTitle());
            } catch (Exception e) {
                log.error("강의 추천 정보 파싱 중 오류 발생: {}", e.getMessage());
            }
        }

        // 기본 패턴으로 매칭되지 않으면 대체 패턴 시도
        if (!foundMatches) {
            Matcher altMatcher = altPattern.matcher(aiResponseText);
            count = 0;

            while (altMatcher.find() && count < 3) {
                LectureRecommendation recommendation = new LectureRecommendation();

                try {
                    // 패턴에 따라 다르게 처리
                    recommendation.setLectureId(Long.parseLong(altMatcher.group(2).trim()));
                    recommendation.setTitle(altMatcher.group(3).trim());
                    recommendation.setInstructorName(altMatcher.group(4).trim());

                    // 경력 정보가 있으면 추출, 없으면 빈 문자열
                    if (altMatcher.groupCount() >= 5 && altMatcher.group(5) != null) {
                        recommendation.setInstructorCareer(altMatcher.group(5).trim());
                    } else {
                        recommendation.setInstructorCareer("게임 강사");
                    }

                    // 추천 이유 추출
                    if (altMatcher.groupCount() >= 6 && altMatcher.group(6) != null) {
                        recommendation.setRecommendationReason(altMatcher.group(6).trim());
                    } else {
                        recommendation.setRecommendationReason("회원님의 실력 수준과 학습 목표에 적합한 강의입니다.");
                    }

                    // 기대 효과는 추출이 어려울 수 있으므로 기본값 설정
                    recommendation.setExpectedEffect("게임 실력 향상 및 티어 상승에 도움이 됩니다.");

                    recommendations.add(recommendation);
                    count++;
                    log.info("대체 패턴 - 추천 강의 #{} 파싱 성공: ID={}, 제목={}", count, recommendation.getLectureId(), recommendation.getTitle());
                } catch (Exception e) {
                    log.error("대체 패턴 - 강의 추천 정보 파싱 중 오류 발생: {}", e.getMessage());
                }
            }
        }

        // 종합 추천 의견 추출
        Pattern overallPattern = Pattern.compile("종합\\s*추천\\s*의견:?\\s*([^\\n]+)", Pattern.DOTALL);
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
                        log.info("강의 ID {} 추가 정보 보강 완료: 이미지={}, 가격={}",
                                recommendation.getLectureId(), recommendation.getImageUrl(), recommendation.getPrice());
                    });
        }
    }

    /**
     * 추천 결과를 정제하는 로직 추가
     */
    private List<LectureRecommendation> refineRecommendations(List<LectureRecommendation> recommendations) {
        if (recommendations == null) {
            return new ArrayList<>();
        }

        List<LectureRecommendation> refined = new ArrayList<>();

        for (LectureRecommendation rec : recommendations) {
            // 불필요한 텍스트 정리
            rec.setRecommendationReason(cleanupText(rec.getRecommendationReason()));
            rec.setExpectedEffect(cleanupText(rec.getExpectedEffect()));

            // 빈 필드 채우기
            if (rec.getInstructorName() == null || rec.getInstructorName().trim().isEmpty()) {
                rec.setInstructorName("전문 코치");
            }

            if (rec.getInstructorCareer() == null || rec.getInstructorCareer().trim().isEmpty()) {
                rec.setInstructorCareer("게임 강사");
            }

            refined.add(rec);
        }

        return refined;
    }

    /**
     * 텍스트 정리 - 불필요한 줄바꿈과 공백 제거
     */
    private String cleanupText(String text) {
        if (text == null) return "";
        return text.replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
}