/*
package com.reboot.survey.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.survey.dto.LectureRecommendation;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.entity.Survey;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reboot.survey.dto.SearchResult;
import com.reboot.survey.dto.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecommendationService {

    private final VectorDbService vectorDbService;
    private final LlmService llmService;
    private final LectureRepository lectureRepository;

    @Autowired
    public RecommendationService(
            VectorDbService vectorDbService,
            LlmService llmService,
            LectureRepository lectureRepository) {
        this.vectorDbService = vectorDbService;
        this.llmService = llmService;
        this.lectureRepository = lectureRepository;
    }

    */
/**
     * 사용자와 설문조사 데이터를 기반으로 맞춤형 강의를 추천합니다.
     * @param member 회원 정보
     * @param survey 설문조사 정보
     * @return 추천된 강의 응답
     *//*

    public RecommendationResponse getRecommendations(Member member, Survey survey) {
        // 1. 설문 데이터를 기반으로 벡터 DB 검색 쿼리 구성
        String searchQuery = buildSearchQuery(survey);

        // 2. 메타데이터 필터 구성 (게임 타입 필터링)
        Map<String, Object> filter = new HashMap<>();
        filter.put("gameType", survey.getGameType().name());

        // 3. 게임 포지션 필터링 (선택적)
        if (survey.getGamePosition() != null && !survey.getGamePosition().isEmpty()) {
            filter.put("position", survey.getGamePosition());
        }

        // 4. 게임 티어/랭크 필터링 (선택적)
        if (survey.getGameTier() != null && !survey.getGameTier().isEmpty()) {
            filter.put("rank", survey.getGameTier());
        }

        // 5. 활성 강의만 필터링
        filter.put("isActive", "true");

        // 6. 하이브리드 검색 실행 (벡터 유사도 + 메타데이터 필터링)
        List<SearchResult> searchResults = vectorDbService.hybridSearch(
                searchQuery,
                filter,
                5  // 상위 5개 결과 검색
        );

        //검색 쿼리와 필터를 분리하는 이유
        //다른 검색 메커니즘 사용
        //검색 쿼리(searchQuery): 벡터 유사도 기반 검색에 사용됩니다. 텍스트를 임베딩하여 "의미적으로 유사한" 항목을 찾습니다.
        //필터(filter): 정확한 값 매칭(exact matching)에 사용됩니다. "game_type이 LOL인 것만" 같은 명확한 조건입니다.
        //성능 최적화
        //먼저 필터로 데이터를 줄인 후 벡터 검색을 수행하면 더 효율적입니다.
        //예: 100,000개 강의 중 "LOL" 게임 타입 5,000개로 줄인 후, 그 중에서 유사도 검색
        //다양한 검색 요구 처리
        //필터: "반드시 포함해야 하는 조건" (예: 특정 게임 타입만 보기)
        //검색 쿼리: "비슷한 내용 찾기" (예: 초보자 탈출에 관한 내용)

        //하이브리드 검색의 핵심 로직은 다음과 같이 작동합니다:
        //텍스트 임베딩 생성: 검색 쿼리를 벡터로 변환합니다. (일반적으로 OpenAI, Cohere 등의 임베딩 모델 사용)
        //필터 조건 구성: 메타데이터 필터를 벡터 DB의 필터 형식으로 변환합니다. (예: Pinecone은 {"$and": {"gameType": {"$eq": "LOL"}}} 형식 사용)
        //벡터 유사도 검색 + 필터링:
        //벡터 DB는 먼저 필터 조건을 만족하는 데이터만 선별합니다.
        //그 다음 선별된 데이터와 쿼리 벡터 간의 코사인 유사도를 계산합니다.
        //유사도 점수가 높은 순으로 상위 N개 결과를 반환합니다.
        //결과 후처리:
        //유사도 임계값 이상인 결과만 선택합니다.
        //벡터 DB 응답을 애플리케이션 도메인 객체로 변환합니다.

        // 7. 검색 결과를 LLM 프롬프트에 사용할 수 있는 형태로 변환
        List<Document> retrievedLectures = fetchLectureDocuments(searchResults);

        // 8. LLM 추천 생성
        String llmRecommendations = llmService.generateRecommendations(member, survey, retrievedLectures);

        // 9. LLM 추천 결과 파싱 및 응답 객체 구성
        RecommendationResponse response = parseRecommendations(llmRecommendations);
        response.setSurveyId(survey.getId());

        return response;
    }

    */
/**
     * 설문조사 데이터를 기반으로 검색 쿼리를 구성합니다.
     *//*

    private String buildSearchQuery(Survey survey) {
        StringBuilder query = new StringBuilder();

        // 기본 게임 정보 추가
        query.append(survey.getGameType().getDisplayName())
                .append(" ")
                .append(survey.getGameTier())
                .append(" ")
                .append(survey.getGamePosition());

        // 학습 목표에 따른 키워드 추가
        switch (survey.getLearningGoal()) {
            case ESCAPE_BEGINNER:
                query.append(" 기본기 메카닉 초보 입문 튜토리얼");
                break;
            case IMPROVE_SKILL:
                query.append(" 실력향상 전략 운영 게임이해");
                break;
            case CLIMB_RANK:
                query.append(" 랭크 승급 티어업 경쟁전 기술향상");
                break;
            case BECOME_PRO:
                query.append(" 프로게이머 고급전략 심화학습");
                break;
            case PLAY_TOGETHER:
                query.append(" 팀플레이 협동 소통 커뮤니케이션");
                break;
            case HOBBY:
                query.append(" 취미 재미 즐거움 여가활동");
                break;
            case EXPERT_ACTIVITY:
                query.append(" 전문가 활동 깊이있는 분석 고급기술");
                break;
        }

        // 선호하는 강의 스타일에 따른 키워드 추가
        switch (survey.getLecturePreference()) {
            case ONE_ON_ONE:
                query.append(" 1대1 개인지도 맞춤형");
                break;
            case OFFLINE:
                query.append(" 오프라인 직접 만남 대면강의");
                break;
            case FILE_PROVIDED:
                query.append(" 파일제공 문서 자료");
                break;
            case REPLAY_ANALYSIS:
                query.append(" 리플레이 분석 피드백");
                break;
            case GROUP:
                query.append(" 그룹강의 다수 함께");
                break;
        }

        return query.toString();
    }

    */
/**
     * 검색 결과에서 강의 문서 정보를 가져옵니다.
     *//*

    private List<Document> fetchLectureDocuments(List<SearchResult> searchResults) {
        List<Document> documents = new ArrayList<>();

        for (SearchResult result : searchResults) {
            Object lectureId = result.getMetadata().get("lectureId");
            Lecture lecture = lectureRepository.findById((Long)lectureId)
                    .orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다: " + lectureId));

            Instructor instructor = lecture.getInstructor();
            Member instructorMember = instructor.getMember();

            StringBuilder content = new StringBuilder();
            content.append("강의 ID: ").append(lecture.getId()).append("\n");
            content.append("제목: ").append(lecture.getInfo().getTitle()).append("\n");
            content.append("게임 타입: ").append(lecture.getInfo().getGameType()).append("\n");
            content.append("설명: ").append(lecture.getInfo().getDescription()).append("\n");
            content.append("가격: ").append(lecture.getInfo().getPrice()).append("\n");
            content.append("강의 시간: ").append(lecture.getInfo().getDuration()).append("\n");
            content.append("랭크/티어: ").append(lecture.getInfo().getRank_()).append("\n");
            content.append("포지션: ").append(lecture.getInfo().getPosition()).append("\n");
            content.append("평균 평점: ").append(lecture.getMetadata().getAverageRating()).append("\n");
            content.append("수강생 수: ").append(lecture.getMetadata().getTotalMembers()).append("\n");
            content.append("리뷰 수: ").append(lecture.getMetadata().getReviewCount()).append("\n");
            content.append("강사 이름: ").append(instructorMember.getName()).append("\n");
            content.append("강사 닉네임: ").append(instructorMember.getNickname()).append("\n");
            content.append("강사 경력: ").append(instructor.getCareer()).append("\n");
            content.append("강사 설명: ").append(instructor.getDescription()).append("\n");
            content.append("강사 평점: ").append(instructor.getRating()).append("\n");
            content.append("강사 리뷰 수: ").append(instructor.getReviewNum()).append("\n");
            content.append("유사도 점수: ").append(result.getScore()).append("\n");

            Map<String, Object> metadata = new HashMap<>(result.getMetadata());

            documents.add(new Document(content.toString(), metadata));
        }

        return documents;
    }

    */
/**
     * LLM이 생성한 추천 결과를 파싱하고 응답 객체로 변환합니다.
     *//*

    private RecommendationResponse parseRecommendations(String llmRecommendations) {
        RecommendationResponse response = new RecommendationResponse();
        List<LectureRecommendation> lectureRecommendations = new ArrayList<>();

        // 여기서 LLM 응답을 파싱하여 추천 강의 목록과 추천 이유를 추출
        // 실제 구현은 LLM 응답 형식에 따라 달라질 수 있음

        // 예시 파싱 로직
        Pattern pattern = Pattern.compile("\\[강의 ID: ([^\\]]+)\\] ([^\n]+)\n\\s*강사: ([^(]+)\\(([^)]+)\\)\n\\s*추천 이유: ([^\n]+)\n\\s*기대 효과: ([^\n]+)");
        Matcher matcher = pattern.matcher(llmRecommendations);

        while (matcher.find() && lectureRecommendations.size() < 5) {
            String lectureIdStr = matcher.group(1);
            String lectureTitle = matcher.group(2);
            String instructorName = matcher.group(3).trim();
            String instructorCareer = matcher.group(4).trim();
            String reason = matcher.group(5).trim();
            String expectedEffect = matcher.group(6).trim();

            LectureRecommendation recommendation = new LectureRecommendation();
            // lectureId를 String에서 Long으로 변환
            try {
                Long lectureId = Long.parseLong(lectureIdStr);
                recommendation.setLectureId(lectureId);
            } catch (NumberFormatException e) {
                // 변환 실패 시 로깅 또는 예외 처리
                continue; // 이 추천은 건너뜀
            }
            recommendation.setTitle(lectureTitle);
            recommendation.setInstructorName(instructorName);
            recommendation.setInstructorCareer(instructorCareer);
            recommendation.setRecommendationReason(reason);
            recommendation.setExpectedEffect(expectedEffect);

            lectureRecommendations.add(recommendation);
        }

        // 종합 추천 의견 추출
        Pattern overallPattern = Pattern.compile("종합 추천 의견: ([^\n]+)");
        Matcher overallMatcher = overallPattern.matcher(llmRecommendations);
        if (overallMatcher.find()) {
            response.setOverallRecommendation(overallMatcher.group(1).trim());
        }

        response.setRecommendations(lectureRecommendations);
        return response;
    }
}
*/
