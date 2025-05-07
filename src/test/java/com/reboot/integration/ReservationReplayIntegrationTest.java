package com.reboot.integration;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.entity.Replay;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import com.reboot.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 불필요한 스터빙 경고 방지
class ReservationReplayIntegrationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private ReplayService replayService;

    @InjectMocks
    private ReservationService reservationService;

    private Member testMember;
    private Member instructorMember;
    private Instructor testInstructor;
    private Lecture testLecture;
    private Reservation testReservation;
    private Replay testReplay;
    private ReplayResponse testReplayResponse;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 설정
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setName("테스트 사용자");

        // 강사의 회원 정보 설정
        instructorMember = new Member();
        instructorMember.setMemberId(2L);
        instructorMember.setName("강사이름");

        // 테스트 강사 설정 (회원 정보 포함)
        testInstructor = new Instructor();
        testInstructor.setInstructorId(1L);
        testInstructor.setMember(instructorMember); // 중요: 강사 객체에 회원 정보 설정

        // 테스트 강의 정보 설정
        LectureInfo lectureInfo = new LectureInfo();
        lectureInfo.setTitle("테스트 강의");

        // 테스트 강의 설정
        testLecture = new Lecture();
        testLecture.setId("lecture1");
        testLecture.setInfo(lectureInfo); // 강의 정보 설정

        // 테스트 예약 설정
        testReservation = Reservation.builder()
                .reservationId(1L)
                .member(testMember)
                .instructor(testInstructor)
                .lecture(testLecture)
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .date(LocalDateTime.now())
                .status("예약완료")
                .build();

        // 테스트 리플레이 설정
        testReplay = Replay.builder()
                .replayId(1L)
                .reservation(testReservation)
                .fileUrl("https://youtube.com/watch?v=abcdefg")
                .date(LocalDateTime.now())
                .build();

        // 테스트 리플레이 응답 설정
        testReplayResponse = new ReplayResponse();
        testReplayResponse.setReplayId(1L);
        testReplayResponse.setReservationId(1L);
        testReplayResponse.setFileUrl("https://youtube.com/watch?v=abcdefg");
        testReplayResponse.setDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("유튜브 URL이 포함된 예약 생성 시 리플레이도 함께 생성되는지 테스트")
    void testCreateReservationWithReplay() {
        // Given
        ReservationRequestDto requestDto = new ReservationRequestDto();
        requestDto.setMemberId(1L);
        requestDto.setInstructorId(1L);
        requestDto.setLectureId("lecture1");
        requestDto.setYoutubeUrl("https://youtube.com/watch?v=abcdefg");
        requestDto.setScheduleDate("2025-05-15");
        requestDto.setRequestDetail("테스트 요청사항");

        // 모킹 설정
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(testInstructor));
        when(lectureRepository.findById("lecture1")).thenReturn(Optional.of(testLecture));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // 리플레이 생성 모킹
        when(replayService.saveReplay(any(ReplayRequest.class))).thenReturn(testReplayResponse);
        when(replayService.getReplay(anyLong())).thenReturn(testReplayResponse);

        // 예약 응답 DTO에 replay 정보가 포함되어 있도록 모킹 설정 (필요한 경우)
        ReservationResponseDto expectedResponse = ReservationResponseDto.builder()
                .reservationId(1L)
                .memberId(1L)
                .memberName("테스트 사용자")
                .instructorId(1L)
                .instructorName("강사이름")
                .lectureId("lecture1")
                .lectureTitle("테스트 강의")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .status("예약완료")
                .replayId(1L)
                .replayUrl("https://youtube.com/watch?v=abcdefg")
                .build();

        // When
        ReservationResponseDto reservationResult = reservationService.createReservation(requestDto);

        // Then
        assertNotNull(reservationResult, "생성된 예약 응답은 null이 아니어야 합니다");

        // ReplayService.saveReplay 호출 검증
        ArgumentCaptor<ReplayRequest> replayRequestCaptor = ArgumentCaptor.forClass(ReplayRequest.class);
        verify(replayService).saveReplay(replayRequestCaptor.capture());

        // 캡처된 ReplayRequest 검증
        ReplayRequest capturedRequest = replayRequestCaptor.getValue();
        assertEquals(1L, capturedRequest.getReservationId(), "저장 요청한 리플레이의 예약 ID가 일치해야 합니다");
        assertEquals("https://youtube.com/watch?v=abcdefg", capturedRequest.getFileUrl(), "저장 요청한 리플레이의 URL이 일치해야 합니다");
    }

    @Test
    @DisplayName("리플레이가 포함된 예약 조회 테스트")
    void testRetrieveReservationWithReplay() {
        // Given
        // 예약에 리플레이를 설정
        Reservation reservationWithReplay = testReservation;
        reservationWithReplay.setReplay(testReplay);

        // 모킹 설정
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationWithReplay));
        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);

        // 예약 응답 DTO에 replay 정보가 포함되어 있도록 모킹 설정 (필요한 경우)
        ReservationResponseDto expectedResponse = ReservationResponseDto.builder()
                .reservationId(1L)
                .memberId(1L)
                .memberName("테스트 사용자")
                .instructorId(1L)
                .instructorName("강사이름")
                .lectureId("lecture1")
                .lectureTitle("테스트 강의")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .status("예약완료")
                .replayId(1L)
                .replayUrl("https://youtube.com/watch?v=abcdefg")
                .build();

        // When
        ReservationResponseDto retrievedReservation = reservationService.getReservation(1L);

        // Then
        assertNotNull(retrievedReservation, "조회된 예약 응답은 null이 아니어야 합니다");
        assertNotNull(retrievedReservation.getReplayId(), "조회된 예약에는 리플레이 ID가 있어야 합니다");

        // 메서드 호출 검증
        verify(reservationRepository).findById(1L);
    }
}