package com.reboot.survey.service;

import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.survey.dto.LectureRecommendation;
import com.reboot.survey.dto.LlmRequest;
import com.reboot.survey.dto.LlmResponse;
import com.reboot.survey.dto.RecommendationResponse;
import com.reboot.survey.entity.Survey;
import com.reboot.survey.entity.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SimplifiedRecommendationService {

    private final Logger log = LoggerFactory.getLogger(SimplifiedRecommendationService.class);
    private final LectureRepository lectureRepository;
    private final LlmClient llmClient;

    private final String SYSTEM_PROMPT = "당신은 게임 강의 추천 시스템입니다. 회원의 설문 데이터와 시스템에서 필터링된 강의 정보를 분석하여 가장 적합한 3개의 강의를 추천해야 합니다.\n\n" +
            "각 추천에는 다음이 포함되어야 합니다:\n" +
            "1. 강의 ID와 제목\n" +
            "2. 강사 이름과 간략한 경력\n" +
            "3. 해당 강의가 이 회원에게 적합한 이유 (설문 데이터와의 연관성)\n" +
            "4. 회원의 게임 실력 향상을 위한 기대 효과\n\n" +
            "프로세스:\n" +
            "1. 회원의 설문 데이터(게임 타입, 티어, 포지션, 학습 목표, 가능 시간, 강의 선호도)를 분석하세요.\n" +
            "2. 필터링된 강의 데이터를 검토하세요.\n" +
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
            "3. [강의 ID: {lecture_id}] {lecture_title}\n" +
            "   ...\n\n" +
            "종합 추천 의견: {overall_recommendation}";

    @Autowired
    public SimplifiedRecommendationService(LectureRepository lectureRepository, LlmClient llmClient) {
        this.lectureRepository = lectureRepository;
        this.llmClient = llmClient;
    }

    /**
     * 사용자와 설문조사 데이터를 기반으로 맞춤형 강의를 추천합니다.
     * @param member 회원 정보
     * @param survey 설문조사 정보
     * @return 추천된 강의 응답
     */
    public RecommendationResponse getRecommendations(Member member, Survey survey) {
        log.info("강의 추천 시작 - 회원: {}, 게임 타입: {}", member.getNickname(), survey.getGameType());
        
        // 1. 회원 설문을 기반으로 강의 필터링
        List<Lecture> filteredLectures = filterLecturesByUserPreferences(survey);
        
        if (filteredLectures.isEmpty()) {
            return getFallbackRecommendations(survey);
        }
        
        // 2. 필터링된 강의 정보를 LLM 프롬프트용으로 변환
        String lecturesData = formatLecturesData(filteredLectures);
        
        // 3. 프롬프트 구성
        String prompt = buildPrompt(member, survey, lecturesData);
        
        // 4. LLM 추천 생성
        LlmRequest request = new LlmRequest();
        request.setSystemPrompt(SYSTEM_PROMPT);
        request.setUserPrompt(prompt);
        request.setResponseFormat(RESPONSE_FORMAT);
        request.setTemperature(0.3f); // 일관된 결과를 위해 낮은 온도 설정
        request.setMaxTokens(2000);
        
        String llmRecommendations;
        try {
            LlmResponse response = llmClient.generateCompletion(request);
            llmRecommendations = response.getCompletion();
        } catch (Exception e) {
            log.error("LLM 추천 생성 실패: {}", e.getMessage());
            return getFallbackRecommendations(survey);
        }
        
        // 5. LLM 추천 결과 파싱 및 응답 객체 구성
        RecommendationResponse response = parseRecommendations(llmRecommendations);
        response.setSurveyId(survey.getId());
        
        return response;
    }
    
    /**
     * 회원의 설문 데이터를 기반으로 강의를 필터링합니다.
     * @param survey 설문조사 정보
     * @return 필터링된 강의 목록
     */
    private List<Lecture> filterLecturesByUserPreferences(Survey survey) {
        // 1. 기본 필터: 게임 타입 (필수)
        List<Lecture> lectures = lectureRepository.findByGameTypeAndActive(survey.getGameType().name(), true);
        
        if (lectures.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. 포지션 필터 (선택적)
        if (survey.getGamePosition() != null && !survey.getGamePosition().isEmpty()) {
            List<Lecture> positionFiltered = lectures.stream()
                    .filter(lecture -> lecture.getPosition() == null || 
                                      lecture.getPosition().equals("ALL") || 
                                      lecture.getPosition().equalsIgnoreCase(survey.getGamePosition()))
                    .collect(Collectors.toList());
            
            // 포지션 필터링 결과가 너무 적으면 무시
            if (positionFiltered.size() >= 3) {
                lectures = positionFiltered;
            }
        }
        
        // 3. 티어/랭크 필터 (선택적)
        if (survey.getGameTier() != null && !survey.getGameTier().isEmpty()) {
            List<Lecture> tierFiltered = lectures.stream()
                    .filter(lecture -> {
                        if (lecture.getRank_() == null) return true;
                        // 강의 랭크에 설문 티어가 포함되어 있는지 확인 (예: "BRONZE,SILVER"에 "BRONZE" 포함)
                        return lecture.getRank_().toUpperCase().contains(survey.getGameTier().toUpperCase());
                    })
                    .collect(Collectors.toList());
            
            // 티어 필터링 결과가 너무 적으면 무시
            if (tierFiltered.size() >= 3) {
                lectures = tierFiltered;
            }
        }
        
        // 4. 학습 목표에 따른 추가 필터링
        if (survey.getLearningGoal() != null) {
            lectures = applyLearningGoalFilter(lectures, survey.getLearningGoal(), survey.getSkillLevel());
        }
        
        // 5. 가능한 시간에 따른 필터링 (강의 시간에 따라)
        if (survey.getAvailableTime() != null) {
            lectures = applyAvailableTimeFilter(lectures, survey.getAvailableTime());
        }
        
        // 6. 결과가 너무 많으면 평점과 리뷰 수 기준으로 정렬
        if (lectures.size() > 10) {
            lectures = sortByRatingAndReviews(lectures);
            // 상위 10개만 선택
            lectures = lectures.subList(0, 10);
        }
        
        return lectures;
    }
    
    /**
     * 학습 목표에 따라 강의를 필터링합니다.
     */
    private List<Lecture> applyLearningGoalFilter(List<Lecture> lectures, LearningGoal learningGoal, SkillLevel skillLevel) {
        // 학습 목표에 따른 키워드
        Set<String> targetKeywords = new HashSet<>();
        
        switch (learningGoal) {
            case ESCAPE_BEGINNER:
                targetKeywords.addAll(Arrays.asList("초보", "입문", "기초", "기본기", "탈출"));
                break;
            case IMPROVE_SKILL:
                targetKeywords.addAll(Arrays.asList("향상", "실력", "개선", "피드백", "메카닉"));
                break;
            case CLIMB_RANK:
                targetKeywords.addAll(Arrays.asList("랭크", "티어", "승급", "탈출", "상승"));
                break;
            case BECOME_PRO:
                targetKeywords.addAll(Arrays.asList("프로", "전문가", "고급", "심화", "마스터"));
                break;
            case PLAY_TOGETHER:
                targetKeywords.addAll(Arrays.asList("팀", "협동", "소통", "커뮤니케이션", "함께"));
                break;
            case HOBBY:
                targetKeywords.addAll(Arrays.asList("취미", "재미", "즐거움", "편안한"));
                break;
            case EXPERT_ACTIVITY:
                targetKeywords.addAll(Arrays.asList("전문", "활동", "분석", "고급", "심도"));
                break;
        }
        
        // 스킬 레벨에 따른 보정
        if (skillLevel != null) {
            switch (skillLevel) {
                case BEGINNER:
                    targetKeywords.addAll(Arrays.asList("초보", "입문", "기초"));
                    break;
                case INTERMEDIATE:
                    targetKeywords.addAll(Arrays.asList("중급", "향상", "개선"));
                    break;
                case ADVANCED:
                    targetKeywords.addAll(Arrays.asList("고급", "심화", "전문"));
                    break;
                case EXPERT:
                    targetKeywords.addAll(Arrays.asList("전문가", "마스터", "프로"));
                    break;
            }
        }
        
        // 키워드 매칭 점수 계산
        final Set<String> keywords = targetKeywords;
        Map<Lecture, Integer> lectureScores = new HashMap<>();
        
        for (Lecture lecture : lectures) {
            int score = 0;
            
            // 제목에서 키워드 확인
            if (lecture.getTitle() != null) {
                for (String keyword : keywords) {
                    if (lecture.getTitle().contains(keyword)) {
                        score += 3; // 제목의 키워드는 가중치 높게
                    }
                }
            }
            
            // 설명에서 키워드 확인
            if (lecture.getDescription() != null) {
                for (String keyword : keywords) {
                    if (lecture.getDescription().contains(keyword)) {
                        score += 1;
                    }
                }
            }
            
            lectureScores.put(lecture, score);
        }
        
        // 점수 기준으로 정렬
        return lectures.stream()
                .sorted((l1, l2) -> Integer.compare(lectureScores.getOrDefault(l2, 0), 
                                                    lectureScores.getOrDefault(l1, 0)))
                .collect(Collectors.toList());
    }
    
    /**
     * 가능한 시간에 따라 강의를 필터링합니다.
     */
    private List<Lecture> applyAvailableTimeFilter(List<Lecture> lectures, AvailableTime availableTime) {
        // 가능한 시간에 따른 강의 시간 제한 (분 단위)
        int maxDuration;
        
        switch (availableTime) {
            case VERY_SHORT:
                maxDuration = 45; // 최대 45분
                break;
            case SHORT:
                maxDuration = 60; // 최대 60분
                break;
            case MEDIUM:
                maxDuration = 90; // 최대 90분
                break;
            case LONG:
                maxDuration = 120; // 최대 120분
                break;
            case VERY_LONG:
                maxDuration = Integer.MAX_VALUE; // 제한 없음
                break;
            default:
                maxDuration = Integer.MAX_VALUE;
        }
        
        // 수강 가능한 시간이 매우 적은 경우에만 필터링 적용
        if (maxDuration < Integer.MAX_VALUE) {
            List<Lecture> timeFiltered = lectures.stream()
                    .filter(lecture -> lecture.getDuration() == null || lecture.getDuration() <= maxDuration)
                    .collect(Collectors.toList());
            
            // 결과가 충분하면 필터링된 결과 사용
            if (timeFiltered.size() >= 3) {
                return timeFiltered;
            }
        }
        
        return lectures;
    }
    
    /**
     * 평점과 리뷰 수를 기준으로 강의를 정렬합니다.
     */
    private List<Lecture> sortByRatingAndReviews(List<Lecture> lectures) {
        return lectures.stream()
                .sorted((l1, l2) -> {
                    // 평점 비교 (높은 순)
                    float rating1 = l1.getAverageRating() != null ? l1.getAverageRating() : 0;
                    float rating2 = l2.getAverageRating() != null ? l2.getAverageRating() : 0;
                    
                    int ratingCompare = Float.compare(rating2, rating1);
                    if (ratingCompare != 0) {
                        return ratingCompare;
                    }
                    
                    // 리뷰 수 비교 (많은 순)
                    int reviews1 = l1.getReviewCount() != null ? l1.getReviewCount() : 0;
                    int reviews2 = l2.getReviewCount() != null ? l2.getReviewCount() : 0;
                    
                    return Integer.compare(reviews2, reviews1);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 검색 결과가 없을 경우 기본 추천을 제공합니다.
     */
    private RecommendationResponse getFallbackRecommendations(Survey survey) {
        RecommendationResponse response = new RecommendationResponse();
        response.setSurveyId(survey.getId());
        response.setOverallRecommendation("현재 귀하의 기준에 맞는 강의가 준비되어 있지 않습니다. 곧 더 많은 강의가 추가될 예정입니다.");
        response.setRecommendations(new ArrayList<>());
        return response;
    }
    
    /**
     * 강의 목록을 LLM 프롬프트용 문자열로 변환합니다.
     */
    private String formatLecturesData(List<Lecture> lectures) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < lectures.size(); i++) {
            Lecture lecture = lectures.get(i);
            sb.append(i + 1).append(". ");
            sb.append("강의 ID: ").append(lecture.getId()).append("\n");
            sb.append("제목: ").append(lecture.getTitle()).append("\n");
            sb.append("게임 타입: ").append(lecture.getGameType()).append("\n");
            sb.append("설명: ").append(lecture.getDescription()).append("\n");
            
            if (lecture.getPrice() != null) {
                sb.append("가격: ").append(lecture.getPrice()).append("원\n");
            }
            
            if (lecture.getDuration() != null) {
                sb.append("강의 시간: ").append(lecture.getDuration()).append("분\n");
            }
            
            if (lecture.getRank_() != null) {
                sb.append("랭크/티어: ").append(lecture.getRank_()).append("\n");
            }
            
            if (lecture.getPosition() != null) {
                sb.append("포지션: ").append(lecture.getPosition()).append("\n");
            }
            
            if (lecture.getAverageRating() != null) {
                sb.append("평균 평점: ").append(lecture.getAverageRating()).append("\n");
            }
            
            if (lecture.getTotalMembers() != null) {
                sb.append("수강생 수: ").append(lecture.getTotalMembers()).append("\n");
            }
            
            if (lecture.getReviewCount() != null) {
                sb.append("리뷰 수: ").append(lecture.getReviewCount()).append("\n");
            }
            
            // 강사 정보
            if (lecture.getInstructor() != null) {
                sb.append("강사 이름: ").append(lecture.getInstructor().getMember().getName()).append("\n");
                
                if (lecture.getInstructor().getMember().getNickname() != null) {
                    sb.append("강사 닉네임: ").append(lecture.getInstructor().getMember().getNickname()).append("\n");
                }
                
                if (lecture.getInstructor().getCareer() != null) {
                    sb.append("강사 경력: ").append(lecture.getInstructor().getCareer()).append("\n");
                }
                
                if (lecture.getInstructor().getDescription() != null) {
                    sb.append("강사 설명: ").append(lecture.getInstructor().getDescription()).append("\n");
                }
                
                if (lecture.getInstructor().getAverageRating() != 0) {
                    sb.append("강사 평점: ").append(lecture.getInstructor().getAverageRating()).append("\n");
                }
                
                if (lecture.getInstructor().getReviewNum() != 0) {
                    sb.append("강사 리뷰 수: ").append(lecture.getInstructor().getReviewNum()).append("\n");
                }
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 설문조사 데이터를 기반으로 LLM 프롬프트를 구성합니다.
     */
    private String buildPrompt(Member member, Survey survey, String lecturesData) {
        // 회원 정보 포맷팅
        String memberInfo = String.format(
                "ID: %d\n이름: %s\n닉네임: %s",
                member.getMemberId(),
                member.getName(),
                member.getNickname()
        );

        // 설문 데이터 포맷팅
        StringBuilder surveyData = new StringBuilder();
        
        // 필수 정보
        surveyData.append("게임 타입: ").append(survey.getGameType().name()).append("\n");
        surveyData.append("실력 수준: ").append(survey.getSkillLevel().name()).append(" (");
        
        // 스킬 레벨 설명 추가
        switch (survey.getSkillLevel()) {
            case BEGINNER:
                surveyData.append("초보자, 기초 단계");
                break;
            case INTERMEDIATE:
                surveyData.append("중급자, 기본기 갖춤");
                break;
            case ADVANCED:
                surveyData.append("상급자, 실력이 좋음");
                break;
            case EXPERT:
                surveyData.append("전문가, 매우 높은 수준");
                break;
        }
        surveyData.append(")\n");
        
        surveyData.append("학습 목표: ").append(survey.getLearningGoal().name()).append(" (");
        
        // 학습 목표 설명 추가
        switch (survey.getLearningGoal()) {
            case ESCAPE_BEGINNER:
                surveyData.append("초보 탈출");
                break;
            case IMPROVE_SKILL:
                surveyData.append("실력 향상");
                break;
            case CLIMB_RANK:
                surveyData.append("랭크 상승");
                break;
            case BECOME_PRO:
                surveyData.append("프로 도전");
                break;
            case PLAY_TOGETHER:
                surveyData.append("함께 플레이");
                break;
            case HOBBY:
                surveyData.append("취미 활동");
                break;
            case EXPERT_ACTIVITY:
                surveyData.append("전문가 활동");
                break;
        }
        surveyData.append(")\n");
        
        surveyData.append("가능한 시간: ").append(survey.getAvailableTime().name()).append(" (");
        
        // 가능 시간 설명 추가
        switch (survey.getAvailableTime()) {
            case VERY_SHORT:
                surveyData.append("하루 30분 미만");
                break;
            case SHORT:
                surveyData.append("하루 1시간 미만");
                break;
            case MEDIUM:
                surveyData.append("하루 1-2시간");
                break;
            case LONG:
                surveyData.append("하루 2-4시간");
                break;
            case VERY_LONG:
                surveyData.append("하루 4시간 이상");
                break;
        }
        surveyData.append(")\n");
        
        surveyData.append("강의 선호도: ").append(survey.getLecturePreference().name()).append(" (");
        
        // 강의 선호도 설명 추가
        switch (survey.getLecturePreference()) {
            case ONE_ON_ONE:
                surveyData.append("1:1 개인 강의 선호");
                break;
            case OFFLINE:
                surveyData.append("오프라인 강의 선호");
                break;
            case FILE_PROVIDED:
                surveyData.append("강의 자료 제공 선호");
                break;
            case REPLAY_ANALYSIS:
                surveyData.append("게임 리플레이 분석 선호");
                break;
            case GROUP:
                surveyData.append("그룹 강의 선호");
                break;
        }
        surveyData.append(")\n");
        
        // 선택적 정보
        if (survey.getGameTier() != null && !survey.getGameTier().isEmpty()) {
            surveyData.append("게임 티어: ").append(survey.getGameTier()).append("\n");
        }
        
        if (survey.getGamePosition() != null && !survey.getGamePosition().isEmpty()) {
            surveyData.append("게임 포지션: ").append(survey.getGamePosition()).append("\n");
        }

        // 최종 프롬프트 구성
        return String.format(
                "다음은 회원 정보와 설문 데이터입니다:\n\n[회원 정보]\n%s\n\n[설문 데이터]\n%s\n\n다음은 필터링된 강의 목록입니다(총 %d개):\n%s\n\n이 회원에게 가장 적합한 3개의 강의를 추천하고, 각각에 대해 추천 이유를 자세히 설명해주세요. 강의 ID를 반드시 포함해 주세요.",
                memberInfo,
                surveyData.toString(),
                lecturesData.split("\n\n").length - 1,
                lecturesData
        );
    }
    
    /**
     * LLM이 생성한 추천 결과를 파싱하고 응답 객체로 변환합니다.
     */
    private RecommendationResponse parseRecommendations(String llmRecommendations) {
        RecommendationResponse response = new RecommendationResponse();
        List<LectureRecommendation> lectureRecommendations = new ArrayList<>();

        // 추천 강의 파싱을 위한 정규식 패턴
        Pattern pattern = Pattern.compile("\\[강의 ID: ([^\\]]+)\\] ([^\n]+)\n\\s*강사: ([^(]+)\\(([^)]+)\\)\n\\s*추천 이유: ([^\n]+)\n\\s*기대 효과: ([^\n]+)");
        Matcher matcher = pattern.matcher(llmRecommendations);

        // 최대 3개까지 추천 결과 파싱
        while (matcher.find() && lectureRecommendations.size() < 3) {
            try {
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
                    log.error("강의 ID 변환 실패: {}", lectureIdStr);
                    continue; // 이 추천은 건너뜀
                }

                recommendation.setTitle(lectureTitle);
                recommendation.setInstructorName(instructorName);
                recommendation.setInstructorCareer(instructorCareer);
                recommendation.setRecommendationReason(reason);
                recommendation.setExpectedEffect(expectedEffect);

                lectureRecommendations.add(recommendation);
            } catch (Exception e) {
                // 파싱 오류 시 해당 항목 건너뜀
                log.error("추천 파싱 오류: {}", e.getMessage());
                continue;
            }
        }

        // 종합 추천 의견 추출
        Pattern overallPattern = Pattern.compile("종합 추천 의견: ([^\n]+)");
        Matcher overallMatcher = overallPattern.matcher(llmRecommendations);
        if (overallMatcher.find()) {
            response.setOverallRecommendation(overallMatcher.group(1).trim());
        } else {
            // 기본 종합 의견 설정
            response.setOverallRecommendation("설문 결과를 바탕으로 맞춤형 강의를 추천해 드립니다.");
        }

        response.setRecommendations(lectureRecommendations);
        return response;
    }
}