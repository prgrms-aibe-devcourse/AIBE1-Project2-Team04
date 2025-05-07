package com.reboot.reservation.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.entity.Replay;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.repository.ReservationRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.reservation.entity.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceIntegrationTest {

    @Mock
    private ReplayService replayService;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 필요한 mock 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(new Member()));
        when(instructorRepository.findById(anyLong())).thenReturn(Optional.of(new Instructor()));
        when(lectureRepository.findById(anyString())).thenReturn(Optional.of(new Lecture()));
    }

    @Test
    void createReservation_WithYoutubeUrl_ShouldCreateReplayToo() {
        // Given
        ReservationRequestDto requestDto = new ReservationRequestDto();
        requestDto.setMemberId(1L);
        requestDto.setInstructorId(1L);
        requestDto.setLectureId("lecture1");
        requestDto.setYoutubeUrl("https://youtube.com/watch?v=abcdefg");

        Reservation savedReservation = new Reservation();
        savedReservation.setReservationId(1L);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReplayResponse replayResponse = new ReplayResponse();
        replayResponse.setReplayId(1L);
        replayResponse.setFileUrl("https://youtube.com/watch?v=abcdefg");
        when(replayService.saveReplay(any(ReplayRequest.class))).thenReturn(replayResponse);

        // When
        ReservationResponseDto result = reservationService.createReservation(requestDto);

        // Then
        verify(replayService).saveReplay(any(ReplayRequest.class));
        assertNotNull(result.getReplayId());
        assertEquals(1L, result.getReplayId());
        assertEquals("https://youtube.com/watch?v=abcdefg", result.getReplayUrl());
    }

    @Test
    void getReservation_WithReplay_ShouldIncludeReplayInfo() {
        // Given
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        Replay replay = new Replay();
        replay.setReplayId(1L);
        replay.setFileUrl("https://youtube.com/watch?v=abcdefg");
        reservation.setReplay(replay);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        ReservationResponseDto result = reservationService.getReservation(reservationId);

        // Then
        assertNotNull(result.getReplayId());
        assertEquals(1L, result.getReplayId());
        assertEquals("https://youtube.com/watch?v=abcdefg", result.getReplayUrl());
    }
}