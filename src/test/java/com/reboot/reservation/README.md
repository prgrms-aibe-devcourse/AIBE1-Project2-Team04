# Reservation 모듈 테스트 가이드

## 개요

Reservation(예약) 모듈은 강의 예약 기능을 담당하는 핵심 모듈입니다. 이 가이드는 Reservation 모듈의 테스트 방법과 테스트 코드 구조에 대해 설명합니다.

## 테스트 클래스 구조

Reservation 모듈 테스트는 다음과 같은 계층 구조로 구성되어 있습니다:

1. **단위 테스트(Service)**: `ReservationServiceTest`
   - 서비스 계층의 메소드별 동작 검증
   - 예약 생성, 조회, 취소 등 핵심 기능 테스트
   - Mockito를 사용한 Repository 의존성 격리

2. **컨트롤러 테스트**: `ReservationControllerTest`
   - 컨트롤러 계층의 API 엔드포인트 검증
   - MockMvc를 활용한 HTTP 요청/응답 테스트
   - 뷰 이름 및 모델 속성 검증

3. **모듈 통합 테스트**: `ReservationModuleTest`
   - Reservation 모듈 내부 통합 기능 검증
   - 외부 모듈 의존성은 Mock으로 대체하여 격리된 환경 구성
   - 핵심 비즈니스 로직 검증 중심

4. **테스트 설정**: `ReservationTestConfig`
   - 테스트 환경 구성을 위한 Spring Configuration
   - Bean 주입 및 Mock 객체 설정
   - 모듈 간 의존성 분리를 위한 설정

## 테스트 데이터 구성

모든 테스트에서 공통으로 사용하는 테스트 데이터는 다음과 같이 구성됩니다:

- **테스트 회원**: 예약을 요청하는 회원 정보
- **테스트 강사**: 강의를 제공하는 강사 정보
- **테스트 강의**: 예약 대상이 되는 강의 정보 (LectureInfo, LectureMetaData 포함)
- **테스트 예약**: 예약 엔티티 객체
- **테스트 DTO**: 요청 및 응답 데이터 객체

특히 `Lecture` 객체는 `LectureInfo`와 `LectureMetaData`를 포함해야 하며, `null` 값이 발생하지 않도록 주의해야 합니다.

## 테스트 실행 방법

```bash
# 전체 모듈 테스트
./gradlew test --tests "com.reboot.reservation.*"

# 개별 테스트 클래스 실행
./gradlew test --tests "com.reboot.reservation.service.ReservationServiceTest"
./gradlew test --tests "com.reboot.reservation.controller.ReservationControllerTest"
./gradlew test --tests "com.reboot.reservation.ReservationModuleTest"
```

## 주요 테스트 케이스

### 1. 예약 생성 테스트
```java
@Test
@DisplayName("예약 생성 성공 테스트")
void createReservationSuccess() {
    // Given
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(instructorRepository.findById(1L)).thenReturn(Optional.of(testInstructor));
    when(lectureRepository.findById("LECTURE-123")).thenReturn(Optional.of(testLecture));
    when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

    // When
    ReservationResponseDto responseDto = reservationService.createReservation(validRequestDto);

    // Then
    assertNotNull(responseDto);
    assertEquals(1L, responseDto.getReservationId());
    assertEquals("홍길동", responseDto.getMemberName());
    assertEquals("롤 초보 탈출 강의", responseDto.getLectureTitle());
    assertEquals("예약완료", responseDto.getStatus());
}
```

### 2. 예약 조회 테스트
```java
@Test
@DisplayName("예약 ID로 조회 성공 테스트")
void getReservationSuccess() {
    // Given
    when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

    // When
    ReservationResponseDto responseDto = reservationService.getReservation(1L);

    // Then
    assertNotNull(responseDto);
    assertEquals(1L, responseDto.getReservationId());
    assertEquals("롤 초보 탈출 강의", responseDto.getLectureTitle());
}
```

### 3. 예약 취소 테스트
```java
@Test
@DisplayName("예약 취소 성공 테스트")
void cancelReservationSuccess() {
    // Given
    when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
    
    Reservation canceledReservation = Reservation.builder()
            .reservationId(1L)
            .member(testMember)
            .instructor(testInstructor)
            .lecture(testLecture)
            .status("취소")
            .cancelReason("개인 사정으로 인한 취소")
            .build();
            
    when(reservationRepository.save(any(Reservation.class))).thenReturn(canceledReservation);

    // When
    ReservationResponseDto responseDto = reservationService.cancelReservation(cancelDto);

    // Then
    assertEquals("취소", responseDto.getStatus());
    assertEquals("개인 사정으로 인한 취소", responseDto.getCancelReason());
}
```

## 주의사항 및 알려진 이슈

### 1. `NullPointerException` 처리
테스트 중 `NullPointerException`이 발생하는 주요 원인:
- `testLecture` 객체에 `LectureInfo`가 설정되지 않은 경우
- Repository mock에서 반환하는 객체에 연관 관계가 제대로 설정되지 않은 경우

이를 방지하기 위해 모든 테스트에서 다음을 확인해야 합니다:
```java
// Lecture 객체에 LectureInfo 설정 확인
LectureInfo lectureInfo = new LectureInfo();
lectureInfo.setTitle("롤 초보 탈출 강의");
// 다른 필드 설정...

testLecture.setInfo(lectureInfo);
```

### 2. 날짜 관련 이슈
`scheduleDate` 필드가 String 타입으로 선언되어 있어 날짜 변환 시 주의가 필요합니다. 항상 "yyyy-MM-dd" 형식을 사용하세요.

### 3. Mock 객체 동작 설정
테스트에서 Mock 객체의 동작을 명확하게 설정해야 합니다:
```java
// when 구문으로 Mock 객체 동작 정의
when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

// 메소드 호출 검증
verify(reservationRepository, times(1)).save(any(Reservation.class));
```

## 테스트 확장 가이드

새로운 기능을 추가할 때 테스트 코드도 함께 작성해야 합니다:

1. 관련 테스트 클래스에 새 테스트 메소드 추가
2. 테스트 데이터 설정 및 Mock 객체 동작 정의
3. 예상 결과 명확히 작성
4. 경계 조건 및 예외 케이스도 테스트

## 테스트 코드 품질 관리

- 테스트 중복 코드는 헬퍼 메소드로 추출
- 명확한 `@DisplayName` 설정으로 테스트 목적 표현
- Given-When-Then 패턴 준수
- 모든 테스트는 독립적으로 실행 가능해야 함
- 테스트 실패 시 명확한 실패 메시지 제공

## 테스트 커버리지 목표

- 서비스 계층: 90% 이상
- 컨트롤러 계층: 85% 이상
- 엔티티 및 DTO: 80% 이상

테스트 커버리지는 Jacoco 플러그인을 사용하여 측정할 수 있습니다.

```bash
./gradlew jacocoTestReport
```

## 테스트 환경 요구사항

- JUnit 5
- Mockito
- Spring Test
- MockMvc

## 문제 해결 및 디버깅

테스트 실패 시 다음 순서로 확인하세요:

1. 테스트 데이터 설정 오류 (특히 null 값 여부)
2. Mock 객체 동작 설정 오류
3. 실제 코드와 테스트 코드 간 불일치
4. 테스트 환경 설정 문제

---

이 테스트 가이드를 철저히 따르면 Reservation 모듈의 모든 기능이 올바르게 동작하는지 검증할 수 있습니다. 질문이나 문제가 있으면 개발팀에 문의하세요.