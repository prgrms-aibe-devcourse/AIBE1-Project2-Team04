# Replay 모듈 독립 테스트

이 디렉토리는 Replay 모듈을 다른 모듈(auth, common, config 등)의 영향 없이 독립적으로 테스트하기 위한 클래스들을 포함하고 있습니다.

## 테스트 구조

- `ReplayTestConfig.java`: 테스트를 위한 스프링 설정 클래스입니다. 다른 모듈에 대한 의존성을 모킹하여 독립적인 테스트 환경을 구성합니다.
- `ReplayServiceTest.java`: Replay 서비스 계층에 대한 단위 테스트입니다.
- `ReplayControllerTest.java`: Replay 컨트롤러 계층에 대한 단위 테스트입니다.
- `ReplayModuleTest.java`: Replay 모듈 전체에 대한 통합 테스트입니다.

## 테스트 실행 방법

### 단일 테스트 클래스 실행

```bash
# 서비스 테스트만 실행
./gradlew test --tests "com.reboot.replay.service.ReplayServiceTest"

# 컨트롤러 테스트만 실행
./gradlew test --tests "com.reboot.replay.controller.ReplayControllerTest"

# 모듈 전체 테스트 실행
./gradlew test --tests "com.reboot.replay.ReplayModuleTest"
```

### 모든 Replay 관련 테스트 실행

```bash
./gradlew test --tests "com.reboot.replay.*"
```

## 테스트 전략

1. **모킹 기반 테스트**: 모든 외부 의존성(Reservation 관련 코드 등)은 Mockito를 사용하여 모킹되어, Replay 모듈만 독립적으로 테스트됩니다.

2. **TestConfiguration 사용**: `ReplayTestConfig`는 스프링의 테스트 구성을 정의하여 모킹된 빈들을 스프링 컨텍스트에 등록합니다.

3. **단위 테스트와 통합 테스트 분리**: 
   - 서비스와 컨트롤러는 개별적으로 단위 테스트됩니다.
   - `ReplayModuleTest`는 모듈 내 컴포넌트 간의 통합을 테스트합니다.

## 참고사항

이 테스트 설정은 다음과 같은 경우에 유용합니다:

1. Replay 모듈만 빠르게 개발하고 테스트하고 싶을 때
2. 다른 모듈의 의존성 문제로 전체 애플리케이션 테스트가 어려울 때
3. Replay 모듈의 기능을 독립적으로 검증하고 싶을 때

## 의존성 문제 해결

만약 테스트 실행 중 의존성 관련 오류가 발생한다면, 다음을 확인하세요:

1. `build.gradle`에 필요한 테스트 의존성이 포함되어 있는지 확인
2. `ReplayTestConfig`에 필요한 모든 의존성이 모킹되었는지 확인
3. 테스트 클래스에 `@Import(ReplayTestConfig.class)` 어노테이션이 있는지 확인
