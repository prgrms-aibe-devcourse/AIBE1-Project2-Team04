# 게임 강의 추천 시스템 (Game Lecture Recommendation System)

이 프로젝트는 사용자의 게임 설문조사 데이터를 기반으로 맞춤형 강의를 추천하는 시스템입니다.

## 프로젝트 개요

사용자가 게임 종류, 실력 수준, 학습 목표, 가능한 학습 시간, 선호하는 강의 형태 등을 선택하면, 
그에 맞는 최적의 강의를 추천해주는 시스템입니다.

## 기술 스택

- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf (템플릿 엔진)
- Lombok
- 외부 LLM API (실제 구현 시 OpenAI, Anthropic 등 사용)

## 주요 컴포넌트 설명

### 1. 간소화된 접근 방식

이 구현은 벡터 데이터베이스와 임베딩 기반 검색을 사용하지 않는 간소화된 접근 방식을 채택했습니다.
대신 직접적인 데이터베이스 쿼리와 LLM을 활용하여 추천 시스템을 구현했습니다.

### 2. 폴더 구조 및 주요 파일

```
com.reboot.survey
  ├── config
  │   └── SurveyConfig.java           # 빈 설정
  ├── controller
  │   └── UpdatedSurveyController.java  # 설문 컨트롤러
  ├── dto
  │   ├── Document.java               # 문서 DTO
  │   ├── LectureRecommendation.java  # 강의 추천 DTO
  │   ├── LlmRequest.java             # LLM 요청 DTO
  │   ├── LlmResponse.java            # LLM 응답 DTO
  │   ├── RecommendationResponse.java # 추천 응답 DTO
  │   ├── SearchResult.java           # 검색 결과 DTO
  │   └── SurveyRequest.java          # 설문 요청 DTO
  ├── entity
  │   ├── Survey.java                 # 설문 엔터티
  │   └── enums                       # 열거형 타입들
  │       ├── AvailableTime.java
  │       ├── GameType.java
  │       ├── LearningGoal.java
  │       ├── LecturePreference.java
  │       └── SkillLevel.java
  ├── exception
  │   └── GlobalExceptionHandler.java  # 예외 처리
  ├── repository
  │   └── SurveyRepository.java        # 설문 레포지토리
  └── service
      ├── LlmClient.java               # LLM 클라이언트 인터페이스
      ├── SimplifiedLlmClientImpl.java # 간소화된 LLM 클라이언트 구현
      ├── SimplifiedRecommendationService.java # 간소화된 추천 서비스
      └── UpdatedSurveyService.java    # 업데이트된 설문 서비스
```

### 3. 주요 클래스 기능

#### 3.1 컨트롤러 (Controller)

- **UpdatedSurveyController**: 설문 프로세스의 모든 단계 처리 및 추천 결과 표시

#### 3.2 서비스 (Service)

- **UpdatedSurveyService**: 설문 데이터 저장 및 추천 서비스 호출
- **SimplifiedRecommendationService**: 강의 추천 로직 핵심 구현
- **SimplifiedLlmClientImpl**: LLM API와의 통신 처리 (현재는 더미 응답 생성)

#### 3.3 엔티티 및 DTO

- **Survey**: 설문 데이터 저장 엔티티
- **SurveyRequest**: 설문 요청 데이터 전송 객체
- **RecommendationResponse**: 추천 응답 데이터 전송 객체
- **LectureRecommendation**: 개별 강의 추천 정보 객체

### 4. 작동 방식

1. 사용자가 설문 단계를 진행하며 선호도 정보 입력
2. 설문 완료 시 `UpdatedSurveyController`가 데이터 수집 및 `UpdatedSurveyService` 호출
3. `UpdatedSurveyService`가 설문 데이터 저장 후 `SimplifiedRecommendationService` 호출
4. `SimplifiedRecommendationService`가 사용자의 설문 데이터에 맞는 강의 검색
5. 검색된 강의 데이터를 LLM에 전달하여 추천 텍스트 생성
6. 생성된 추천 텍스트를 파싱하여 UI에 표시할 추천 데이터 구성
7. 결과를 사용자에게 표시

## 구현 방식 비교

### 기존 접근 방식

기존 접근 방식은 벡터 데이터베이스와 임베딩을 사용하여 의미론적 검색을 구현했습니다:

1. 강의 정보를 문서화하여 임베딩 벡터로 변환
2. 벡터 DB에 저장 및 색인화
3. 설문 데이터를 쿼리로 변환하여 벡터 유사도 검색 실행
4. 검색 결과를 LLM에 전달하여 추천 텍스트 생성

### 현재 간소화된 접근 방식

현재 접근 방식은 기존 DB 기능을 활용한 직접 쿼리 방식으로 단순화했습니다:

1. 설문 데이터 기반으로 DB 쿼리 실행 (예: 게임 타입, 티어, 포지션 필터링)
2. 쿼리 결과 강의 정보를 텍스트로 포맷팅
3. 포맷팅된 텍스트를 LLM에 전달하여 추천 텍스트 생성
4. 생성된 추천 텍스트를 파싱하여 UI에 표시할 추천 데이터 구성

## 장단점

### 간소화된 접근 방식 장점

- 추가 인프라(벡터 DB) 불필요
- 임베딩 API 호출 비용 절감
- 구현 및 유지보수 용이성
- 쿼리 로직의 가독성과 직관성

### 간소화된 접근 방식 단점

- 의미론적 검색 부재로 복잡한 의도 파악 제한됨
- 강의 콘텐츠 내용 기반 검색 어려움
- 대규모 데이터셋에서 확장성 제한

## 향후 개선 방향

1. 실제 LLM API 연동 (OpenAI, Anthropic 등)
2. 추천 알고리즘 고도화 (사용자 피드백 반영)
3. 강의 메타데이터 확장 (태그, 카테고리 등)
4. 사용자 이력 기반 개인화 개선
5. 필요 시 하이브리드 검색 방식 고려 (DB 쿼리 + 의미 검색)
