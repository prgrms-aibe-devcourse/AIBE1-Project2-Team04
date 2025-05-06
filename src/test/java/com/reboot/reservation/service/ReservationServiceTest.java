package com.reboot.reservation.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.entity.LectureMetaData;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Member testMember;
    private Instructor testInstructor;
    private Lecture testLecture;
    private Reservation testReservation;
    private ReservationRequestDto validRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 객체 생성
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setName("홍길동");
        testMember.setUsername("hong");
        testMember.setPassword("password");
        testMember.setEmail("hong@example.com");
        testMember.setNickname("길동이");
        testMember.setRole("USER");

        // 테스트용 강사 회원 객체 생성
        Member instructorMember = new Member();
        instructorMember.setMemberId(2L);
        instructorMember.setName("강사김");
        instructorMember.setUsername("instructor");
        instructorMember.setPassword("password");
        instructorMember.setEmail("instructor@example.com");
        instructorMember.setNickname("강사");
        instructorMember.setRole("INSTRUCTOR");

        // 테스트용 강사 객체 생성
        testInstructor = new Instructor();
        testInstructor.setInstructorId(1L);
        testInstructor.setMember(instructorMember);
        testInstructor.setMemberId(instructorMember.getMemberId());
        testInstructor.setCareer("10년 경력의 프로게이머");
        testInstructor.setDescription("친절하게 가르칩니다");
        testInstructor.setReviewNum(5);
        testInstructor.setRating(4.5);

        // 테스트용 강의 정보 객체 생성
        LectureInfo lectureInfo = new LectureInfo();
        lectureInfo.setTitle("롤 초보 탈출 강의");
        lectureInfo.setDescription("초보도 쉽게 배울 수 있는 강의입니다");
        lectureInfo.setGameType("League of Legends");
        lectureInfo.setPrice(new BigDecimal("50000"));
        lectureInfo.setImageUrl("image.jpg");
        lectureInfo.setDuration(60);
        lectureInfo.setRank_("Gold");
        lectureInfo.setPosition("Mid");

        // 테스트용 강의 메타데이터 객체 생성
        LectureMetaData lectureMetaData = new LectureMetaData();
        lectureMetaData.setAverageRating(4.5f);
        lectureMetaData.setTotalMembers(10);
        lectureMetaData.setReviewCount(5);
        lectureMetaData.setCreatedAt(LocalDateTime.now());
        lectureMetaData.setUpdatedAt(LocalDateTime.now());
        lectureMetaData.setActive(true);

        // 테스트용 강의 객체 생성
        testLecture = Lecture.builder()
                .id("LECTURE-123")
                .instructor(testInstructor)
                .info(lectureInfo)  // LectureInfo 설정 추가
                .metadata(lectureMetaData)  // LectureMetaData 설정 추가
                .build();

        // 테스트용 예약 객체 생성
        testReservation = Reservation.builder()
                .reservationId(1L)
                .member(testMember)
                .instructor(testInstructor)
                .lecture(testLecture)
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")  // LocalDate로 변환
                .date(LocalDateTime.now())
                .status("예약완료")
                .build();

        // 유효한 요청 DTO 생성
        validRequestDto = ReservationRequestDto.builder()
                .memberId(1L)
                .instructorId(1L)
                .lectureId("LECTURE-123")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .build();
    }

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
        assertEquals("강사김", responseDto.getInstructorName());
        assertEquals("LECTURE-123", responseDto.getLectureId());
        assertEquals("롤 초보 탈출 강의", responseDto.getLectureTitle());  // 강의 제목 추가
        assertEquals("테스트 요청사항", responseDto.getRequestDetail());
        assertEquals("2025-05-15", responseDto.getScheduleDate());
        assertEquals("예약완료", responseDto.getStatus());

        verify(memberRepository, times(1)).findById(1L);
        verify(instructorRepository, times(1)).findById(1L);
        verify(lectureRepository, times(1)).findById("LECTURE-123");
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 예약 생성 실패 테스트")
    void createReservationWithNonExistingMember() {
        // Given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());
        ReservationRequestDto invalidRequest = ReservationRequestDto.builder()
                .memberId(999L)
                .instructorId(1L)
                .lectureId("LECTURE-123")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> reservationService.createReservation(invalidRequest));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 강사로 예약 생성 실패 테스트")
    void createReservationWithNonExistingInstructor() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(instructorRepository.findById(999L)).thenReturn(Optional.empty());
        ReservationRequestDto invalidRequest = ReservationRequestDto.builder()
                .memberId(1L)
                .instructorId(999L)
                .lectureId("LECTURE-123")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> reservationService.createReservation(invalidRequest));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 강의로 예약 생성 실패 테스트")
    void createReservationWithNonExistingLecture() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(testInstructor));
        when(lectureRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());
        ReservationRequestDto invalidRequest = ReservationRequestDto.builder()
                .memberId(1L)
                .instructorId(1L)
                .lectureId("NONEXISTENT")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> reservationService.createReservation(invalidRequest));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("null memberId로 예약 생성 실패 테스트")
    void createReservationWithNullMemberId() {
        // Given
        ReservationRequestDto invalidRequest = ReservationRequestDto.builder()
                .memberId(null)
                .instructorId(1L)
                .lectureId("LECTURE-123")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(invalidRequest));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

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
        assertEquals("홍길동", responseDto.getMemberName());
        assertEquals("LECTURE-123", responseDto.getLectureId());
        assertEquals("롤 초보 탈출 강의", responseDto.getLectureTitle());  // 강의 제목 확인 추가
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID로 조회 실패 테스트")
    void getReservationNotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> reservationService.getReservation(999L));
    }

    @Test
    @DisplayName("회원 ID로 예약 목록 조회 테스트")
    void getReservationsByMemberSuccess() {
        // Given
        // 두 번째 예약에도 동일한 Lecture 객체 (LectureInfo가 설정된) 사용
        Reservation secondReservation = Reservation.builder()
                .reservationId(2L)
                .member(testMember)
                .instructor(testInstructor)
                .lecture(testLecture)  // 동일한 testLecture 사용 (LectureInfo 설정됨)
                .requestDetail("두 번째 테스트 요청사항")
                .scheduleDate("2025-06-15")
                .date(LocalDateTime.now())
                .status("예약완료")
                .build();

        List<Reservation> reservationList = Arrays.asList(testReservation, secondReservation);
        when(reservationRepository.findByMemberMemberId(1L)).thenReturn(reservationList);

        // When
        List<ReservationResponseDto> responseDtos = reservationService.getReservationsByMember(1L);

        // Then
        assertNotNull(responseDtos);
        assertEquals(2, responseDtos.size());
        assertEquals(1L, responseDtos.get(0).getReservationId());
        assertEquals(2L, responseDtos.get(1).getReservationId());
        assertEquals("롤 초보 탈출 강의", responseDtos.get(0).getLectureTitle());  // 강의 제목 확인
        assertEquals("롤 초보 탈출 강의", responseDtos.get(1).getLectureTitle());  // 강의 제목 확인
    }

    @Test
    @DisplayName("예약 취소 성공 테스트")
    void cancelReservationSuccess() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        Reservation canceledReservation = Reservation.builder()
                .reservationId(1L)
                .member(testMember)
                .instructor(testInstructor)
                .lecture(testLecture)  // 동일한 testLecture 사용 (LectureInfo 설정됨)
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .date(LocalDateTime.now())
                .status("취소")
                .cancelReason("개인 사정으로 인한 취소")
                .build();

        when(reservationRepository.save(any(Reservation.class))).thenReturn(canceledReservation);

        ReservationCancelDto cancelDto = ReservationCancelDto.builder()
                .reservationId(1L)
                .cancelReason("개인 사정으로 인한 취소")
                .build();

        // When
        ReservationResponseDto responseDto = reservationService.cancelReservation(cancelDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getReservationId());
        assertEquals("취소", responseDto.getStatus());
        assertEquals("개인 사정으로 인한 취소", responseDto.getCancelReason());
        assertEquals("롤 초보 탈출 강의", responseDto.getLectureTitle());  // 강의 제목 확인 추가

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 예약 취소 실패 테스트")
    void cancelReservationNotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        ReservationCancelDto cancelDto = ReservationCancelDto.builder()
                .reservationId(999L)
                .cancelReason("개인 사정으로 인한 취소")
                .build();

        // When & Then
        assertThrows(RuntimeException.class, () -> reservationService.cancelReservation(cancelDto));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}