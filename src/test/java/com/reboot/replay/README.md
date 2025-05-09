# 테스트 코드로 더 견고한 프로젝트 만들기: Replay 모듈 테스트 가이드

## 들어가며

안녕하세요, 개발자 여러분! 이 문서는 Replay 모듈을 독립적으로 테스트하는 방법에 대한 가이드입니다. 테스트 코드는 단순히 코드 품질을 검증하는 것을 넘어, 개발 속도를 높이고 리팩토링을 자신있게 할 수 있게 도와주는 든든한 안전망입니다. 이 가이드를 통해 테스트의 즐거움을 경험해보세요!

## 테스트의 장점

🚀 **빠른 개발 속도** - 다른 모듈에 의존하지 않고 독립적으로 Replay 모듈만 개발할 수 있습니다.  
🛡️ **안전한 리팩토링** - 코드 변경 후에도 테스트가 통과하면 기능이 올바르게 동작한다는 확신을 가질 수 있습니다.  
🔍 **버그 조기 발견** - 프로덕션 환경에 배포하기 전에 문제를 미리 발견할 수 있습니다.  
📚 **살아있는 문서** - 테스트 코드는 모듈의 동작 방식을 설명하는 좋은 예제가 됩니다.  
😌 **스트레스 감소** - "이 코드가 제대로 동작할까?" 하는 불안감에서 벗어날 수 있습니다.

## 테스트 구성

우리의 테스트 환경은 세 가지 주요 계층으로 구성되어 있습니다:

### 1. ReplayTestConfig

모든 마법은 여기서 시작됩니다! 이 설정 클래스는 테스트에 필요한 모든 의존성을 모킹(가짜로 만들기)하여 Replay 모듈을 완전히 독립적으로 테스트할 수 있게 해줍니다.

```java
@Configuration
@Profile("test")
public class ReplayTestConfig {
    
    @Bean
    @Primary
    public ReplayRepository replayRepository() {
        return Mockito.mock(ReplayRepository.class);
    }
    
    @Bean
    @Primary
    public ReservationRepository reservationRepository() {
        return Mockito.mock(ReservationRepository.class);
    }
    
    // 기타 필요한 의존성들...
    
    @Bean
    public ReplayService replayService() {
        return new ReplayServiceImpl(
            replayRepository(), 
            reservationRepository()
            // 기타 필요한 의존성들...
        );
    }
}
```

### 2. 단위 테스트

개별 컴포넌트의 동작을 검증합니다:

- `ReplayServiceTest.java`: YouTube URL 검증, 영상 정보 처리 등 비즈니스 로직 테스트
- `ReplayControllerTest.java`: 리플레이 업로드, 조회, 수정, 삭제 등 HTTP 요청 처리 테스트

### 3. 통합 테스트

- `ReplayModuleTest.java`: 모듈 내 컴포넌트 간의 상호작용 테스트

## 테스트 실행 방법

테스트 실행은 정말 간단합니다! 다음 명령어를 사용해보세요:

```bash
# 서비스 테스트만 실행
./gradlew test --tests "com.reboot.replay.service.ReplayServiceTest"

# 컨트롤러 테스트만 실행
./gradlew test --tests "com.reboot.replay.controller.ReplayControllerTest"

# 모듈 전체 테스트 실행
./gradlew test --tests "com.reboot.replay.ReplayModuleTest"

# 모든 Replay 관련 테스트 실행
./gradlew test --tests "com.reboot.replay.*"
```

## 테스트 작성 실전 팁

### 1. BDD 스타일의 테스트 작성

BDD(Behavior-Driven Development) 스타일을 사용하면 테스트가 더 읽기 쉽고 의도가 명확해집니다:

```java
@Test
@DisplayName("유튜브 URL이 올바르게 변환되어야 한다")
void youtubeUrlConversion() {
    // Given (준비): 테스트할 유튜브 URL 준비
    String youtubeUrl = "https://www.youtube.com/watch?v=abc123";
    
    // 비디오 ID 추출과 임베드 URL 생성을 위한 응답 DTO 생성
    ReplayResponse response = ReplayResponse.builder()
            .fileUrl(youtubeUrl)
            .build();
    
    // When (실행): 메서드 실행
    String videoId = response.getYoutubeVideoId();
    String embedUrl = response.getYoutubeEmbedUrl();
    
    // Then (검증): 결과 확인
    assertEquals("abc123", videoId);
    assertEquals("https://www.youtube.com/embed/abc123", embedUrl);
}
```

### 2. 테스트 데이터 팩토리 사용

테스트 데이터 생성을 위한 팩토리 메서드를 만들면 테스트 코드가 간결해집니다:

```java
private Replay createTestReplay(Long id, String youtubeUrl) {
    Reservation reservation = new Reservation();
    reservation.setReservationId(1L);
    
    return Replay.builder()
            .replayId(id)
            .reservation(reservation)
            .fileUrl(youtubeUrl)
            .date(LocalDateTime.now())
            .build();
}
```

### 3. 테스트 가독성 높이기

`@DisplayName`과 주석을 활용하여 테스트의 의도를 명확하게 표현하세요:

```java
@Test
@DisplayName("유효하지 않은 YouTube URL로 리플레이 저장 시 예외가 발생해야 한다")
void saveReplayWithInvalidUrl() {
    // 유효하지 않은 URL을 가진 요청 객체
    ReplayRequest invalidRequest = ReplayRequest.builder()
            .reservationId(1L)
            .fileUrl("https://www.invalid-url.com")
            .build();
    
    // 예외가 발생하는지 확인
    assertThrows(IllegalArgumentException.class, 
        () -> replayService.saveReplay(invalidRequest));
    
    // 리포지토리 메서드가 호출되지 않았는지 확인
    verify(replayRepository, never()).save(any(Replay.class));
}
```

## 테스트 시 자주 발생하는 문제 해결

### 1. 모의 객체(Mock) 동작 문제

**문제**: 모의 객체가 예상대로 동작하지 않습니다.  
**해결**: `verify()` 메서드를 사용하여 모의 객체의 메서드가 호출되었는지 확인하세요:

```java
// 리플레이 저장 메서드가 정확히 1번 호출되었는지 확인
verify(replayRepository, times(1)).save(any(Replay.class));

// 리플레이 삭제 메서드가 호출되지 않았는지 확인
verify(replayRepository, never()).delete(any(Replay.class));
```

### 2. 테스트 간 간섭 문제

**문제**: 테스트 간에 상태가 공유되어 간섭이 발생합니다.  
**해결**: 각 테스트 메서드 전에 `@BeforeEach`를 사용하여 모의 객체를 초기화하세요:

```java
@BeforeEach
void setUp() {
    // 모든 모의 객체 초기화
    reset(replayRepository, reservationRepository);
    
    // 테스트 데이터 설정
    testReservation = new Reservation();
    testReservation.setReservationId(1L);
    
    testReplay = createTestReplay(1L, "https://www.youtube.com/watch?v=abc123");
}
```

### 3. YouTube URL 검증 관련 문제

**문제**: YouTube URL 형식이 다양하여 처리가 어렵습니다.  
**해결**: 다양한 URL 형식을 테스트하여 모든 케이스를 커버하세요:

```java
@Test
@DisplayName("다양한 YouTube URL 형식에서 영상 ID가 올바르게 추출되어야 한다")
void extractVideoIdFromVariousFormats() {
    // 표준 URL
    ReplayResponse response1 = ReplayResponse.builder()
            .fileUrl("https://www.youtube.com/watch?v=abc123")
            .build();
    
    // 단축 URL
    ReplayResponse response2 = ReplayResponse.builder()
            .fileUrl("https://youtu.be/def456")
            .build();
    
    // 매개변수가 있는 URL
    ReplayResponse response3 = ReplayResponse.builder()
            .fileUrl("https://www.youtube.com/watch?v=ghi789&feature=share")
            .build();
    
    assertEquals("abc123", response1.getYoutubeVideoId());
    assertEquals("def456", response2.getYoutubeVideoId());
    assertEquals("ghi789", response3.getYoutubeVideoId());
}
```

## 마무리

테스트 코드는 단순한 코드 검증 도구가 아니라, 더 나은 설계를 이끌어내는 도구이기도 합니다. "테스트하기 어려운 코드는 설계가 잘못된 코드일 수 있다"는 말이 있듯이, 테스트를 작성하면서 코드의 품질을 높여보세요.

Replay 모듈의 테스트는 특히 YouTube URL 처리와 관련된 로직을 검증하는 데 중요합니다. 다양한 URL 형식을 테스트하고, 임베드 URL 생성 기능이 올바르게 동작하는지 확인하세요.

이 가이드가 여러분의 테스트 여정에 도움이 되길 바랍니다. 테스트와 함께라면, 더 자신감 있게 코드를 변경하고, 더 빠르게 개발하며, 더 안정적인 소프트웨어를 만들 수 있을 것입니다.

행복한 테스팅 되세요! 🎯✨