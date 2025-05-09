package com.reboot.reservation.controller;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.entity.LectureMetaData;
import com.reboot.lecture.service.LectureService;
import com.reboot.auth.service.MemberService;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private LectureService lectureService;

    @Mock
    private MemberService memberService;

    @Mock
    private ReplayService replayService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;
    private ReservationResponseDto testReservationResponse;
    private Member testMember;
    private Member instructorMember;
    private Instructor testInstructor;
    private Lecture testLecture;
    private ReplayResponse testReplayResponse;

    @BeforeEach
    void setUp() {

        reservationController = new ReservationController(
                reservationService,
                lectureService,
                memberService,
                replayService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();

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
        instructorMember = new Member();
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
                .id(1L)
                .instructor(testInstructor)
                .info(lectureInfo)
                .metadata(lectureMetaData)
                .build();

        // 테스트용 응답 객체 생성
        testReservationResponse = ReservationResponseDto.builder()
                .reservationId(1L)
                .memberId(1L)
                .memberName("홍길동")
                .instructorId(1L)
                .instructorName("강사김")
                .lectureId(1L)
                .lectureTitle("롤 초보 탈출 강의")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .date(LocalDateTime.now())
                .status("예약완료")
                .build();

        // 테스트용 리플레이 응답 객체 생성
        testReplayResponse = new ReplayResponse();
        testReplayResponse.setReplayId(1L);
        testReplayResponse.setReservationId(1L);
        testReplayResponse.setFileUrl("https://youtube.com/watch?v=abcdefg");
        testReplayResponse.setDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("예약 폼 페이지 접근 테스트")
    void reservationFormTest() throws Exception {
        // Member, Lecture 서비스 모킹
        when(memberService.getMember(anyLong())).thenReturn(testMember);
        when(lectureService.getLecture(anyLong())).thenReturn(testLecture);

        mockMvc.perform(get("/reservation/new")
                        .param("lectureId", "1")
                        .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("member", "lecture", "reservationRequestDto"))
                .andExpect(view().name("reservation/reservationForm"));

        verify(memberService, times(1)).getMember(1L);
        verify(lectureService, times(1)).getLecture(1L);
    }

    @Test
    @DisplayName("예약 생성 성공 테스트")
    void createReservationSuccessTest() throws Exception {
        // 예약 생성 서비스 모킹
        when(reservationService.createReservation(any(ReservationRequestDto.class)))
                .thenReturn(testReservationResponse);

        mockMvc.perform(post("/reservation")
                        .param("memberId", "1")
                        .param("instructorId", "1")
                        .param("lectureId", "LECTURE-123")
                        .param("requestDetail", "테스트 요청사항")
                        .param("scheduleDate", "2025-05-15"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservation"))
                .andExpect(view().name("reservation/reservationResult"));

        verify(reservationService, times(1)).createReservation(any(ReservationRequestDto.class));
    }

    @Test
    @DisplayName("예약 생성 시 리플레이 정보 포함 테스트")
    void createReservationWithReplayTest() throws Exception {
        // 리플레이 정보가 포함된 응답 설정
        ReservationResponseDto responseWithReplay = testReservationResponse.toBuilder()
                .replayId(1L)
                .replayUrl("https://youtube.com/watch?v=abcdefg")
                .build();

        when(reservationService.createReservation(any(ReservationRequestDto.class)))
                .thenReturn(responseWithReplay);

        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("memberId", "1")
                        .param("instructorId", "1")
                        .param("lectureId", "LECTURE-123")
                        .param("requestDetail", "테스트 요청사항")
                        .param("scheduleDate", "2025-05-15")
                        .param("youtubeUrl", "https://youtube.com/watch?v=abcdefg"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservation"))
                .andExpect(model().attributeExists("replay"))
                .andExpect(view().name("reservation/reservationResult"));

        verify(reservationService, times(1)).createReservation(any(ReservationRequestDto.class));
        verify(replayService, times(1)).getReplay(1L);
    }

    @Test
    @DisplayName("예약 취소 폼 접근 테스트")
    void cancelFormTest() throws Exception {
        mockMvc.perform(get("/reservation/{id}/cancel", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservationCancelDto"))
                .andExpect(view().name("reservation/reservationCancel"));
    }

    @Test
    @DisplayName("예약 취소 성공 테스트")
    void cancelReservationSuccessTest() throws Exception {
        // 예약 취소 서비스 모킹
        ReservationResponseDto canceledReservation = ReservationResponseDto.builder()
                .reservationId(1L)
                .memberId(1L)
                .memberName("홍길동")
                .instructorId(1L)
                .instructorName("강사김")
                .lectureId(1L)
                .lectureTitle("롤 초보 탈출 강의")
                .requestDetail("테스트 요청사항")
                .scheduleDate("2025-05-15")
                .date(LocalDateTime.now())
                .status("취소")
                .cancelReason("개인 사정으로 인한 취소")
                .build();

        when(reservationService.cancelReservation(any(ReservationCancelDto.class)))
                .thenReturn(canceledReservation);

        mockMvc.perform(post("/reservation/{id}/cancel", 1L)
                        .param("reservationId", "1")
                        .param("cancelReason", "개인 사정으로 인한 취소"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservation"))
                .andExpect(view().name("reservation/reservationResult"));

        verify(reservationService, times(1)).cancelReservation(any(ReservationCancelDto.class));
    }

    @Test
    @DisplayName("예약 상세 조회 테스트")
    void getReservationTest() throws Exception {
        // 예약 조회 서비스 모킹
        when(reservationService.getReservation(1L)).thenReturn(testReservationResponse);

        mockMvc.perform(get("/reservation/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservation"))
                .andExpect(view().name("reservation/reservationDetail"));

        verify(reservationService, times(1)).getReservation(1L);
    }

    @Test
    @DisplayName("리플레이가 있는 예약 상세 조회 테스트")
    void getReservationWithReplayTest() throws Exception {
        // 리플레이 정보가 포함된 응답 설정
        ReservationResponseDto responseWithReplay = testReservationResponse.toBuilder()
                .replayId(1L)
                .replayUrl("https://youtube.com/watch?v=abcdefg")
                .build();

        when(reservationService.getReservation(1L)).thenReturn(responseWithReplay);
        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);

        mockMvc.perform(get("/reservation/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservation"))
                .andExpect(model().attributeExists("replay"))
                .andExpect(view().name("reservation/reservationDetail"));

        verify(reservationService, times(1)).getReservation(1L);
        verify(replayService, times(1)).getReplay(1L);
    }

    @Test
    @DisplayName("회원별 예약 목록 조회 테스트")
    void getReservationsByMemberTest() throws Exception {
        // 회원별 예약 목록 조회 서비스 모킹
        List<ReservationResponseDto> reservationList = Arrays.asList(
                testReservationResponse,
                ReservationResponseDto.builder()
                        .reservationId(2L)
                        .memberId(1L)
                        .memberName("홍길동")
                        .instructorId(1L)
                        .instructorName("강사김")
                        .lectureId(2L)
                        .lectureTitle("JPA 강의")
                        .requestDetail("두 번째 테스트 요청사항")
                        .scheduleDate("2025-06-15")
                        .date(LocalDateTime.now())
                        .status("예약완료")
                        .build()
        );

        when(reservationService.getReservationsByMember(1L)).thenReturn(reservationList);

        mockMvc.perform(get("/reservation/member/{memberId}", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reservations"))
                .andExpect(view().name("reservation/reservationList"));

        verify(reservationService, times(1)).getReservationsByMember(1L);
    }
}