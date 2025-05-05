package com.reboot.replay.service;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.entity.Replay;
import com.reboot.replay.repository.ReplayRepository;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplayServiceTest {

    @Mock
    private ReplayRepository replayRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReplayService replayService;

    private Reservation testReservation;
    private Replay testReplay;
    private ReplayRequest validRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 예약 객체 생성
        testReservation = Reservation.builder()
                .reservationId(1L)
                .build();

        // 테스트용 리플레이 객체 생성
        testReplay = Replay.builder()
                .replayId(1L)
                .reservation(testReservation)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .date(LocalDateTime.now())
                .build();

        // 유효한 요청 객체
        validRequest = ReplayRequest.builder()
                .reservationId(1L)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .build();
    }

    @Test
    @DisplayName("리플레이 저장 성공 테스트")
    void saveReplaySuccess() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(replayRepository.save(any(Replay.class))).thenReturn(testReplay);

        // When
        ReplayResponse response = replayService.saveReplay(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getReplayId());
        assertEquals(1L, response.getReservationId());
        assertEquals("https://www.youtube.com/watch?v=abc123", response.getFileUrl());
        verify(replayRepository, times(1)).save(any(Replay.class));
    }

    @Test
    @DisplayName("존재하지 않는 예약으로 리플레이 저장 실패 테스트")
    void saveReplayWithNonExistingReservation() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());
        ReplayRequest invalidRequest = ReplayRequest.builder()
                .reservationId(999L)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> replayService.saveReplay(invalidRequest));
        verify(replayRepository, never()).save(any(Replay.class));
    }

    @Test
    @DisplayName("유효하지 않은 URL로 리플레이 저장 실패 테스트")
    void saveReplayWithInvalidUrl() {
        // Given
        ReplayRequest invalidRequest = ReplayRequest.builder()
                .reservationId(1L)
                .fileUrl("https://www.invalid-url.com")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> replayService.saveReplay(invalidRequest));
        verify(reservationRepository, never()).findById(any());
        verify(replayRepository, never()).save(any());
    }

    @Test
    @DisplayName("리플레이 조회 성공 테스트")
    void getReplaySuccess() {
        // Given
        when(replayRepository.findById(1L)).thenReturn(Optional.of(testReplay));

        // When
        ReplayResponse response = replayService.getReplay(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getReplayId());
        assertEquals(1L, response.getReservationId());
        assertEquals("https://www.youtube.com/watch?v=abc123", response.getFileUrl());
    }

    @Test
    @DisplayName("존재하지 않는 리플레이 조회 실패 테스트")
    void getReplayNotFound() {
        // Given
        when(replayRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> replayService.getReplay(999L));
    }

    @Test
    @DisplayName("예약 ID로 리플레이 목록 조회 테스트")
    void getReplaysByReservationId() {
        // Given
        Replay replay1 = Replay.builder()
                .replayId(1L)
                .reservation(testReservation)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .date(LocalDateTime.now())
                .build();

        Replay replay2 = Replay.builder()
                .replayId(2L)
                .reservation(testReservation)
                .fileUrl("https://www.youtube.com/watch?v=def456")
                .date(LocalDateTime.now())
                .build();

        when(replayRepository.findByReservationReservationId(1L)).thenReturn(Arrays.asList(replay1, replay2));

        // When
        List<ReplayResponse> responses = replayService.getReplaysByReservationId(1L);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getReplayId());
        assertEquals(2L, responses.get(1).getReplayId());
    }

    @Test
    @DisplayName("리플레이 수정 성공 테스트")
    void updateReplaySuccess() {
        // Given
        when(replayRepository.findById(1L)).thenReturn(Optional.of(testReplay));
        
        ReplayRequest updateRequest = ReplayRequest.builder()
                .reservationId(1L) // 예약 ID는 변경되지 않음
                .fileUrl("https://www.youtube.com/watch?v=xyz789") // 새 URL
                .build();
        
        Replay updatedReplay = Replay.builder()
                .replayId(1L)
                .reservation(testReservation)
                .fileUrl("https://www.youtube.com/watch?v=xyz789")
                .date(LocalDateTime.now())
                .build();
                
        when(replayRepository.save(any(Replay.class))).thenReturn(updatedReplay);

        // When
        ReplayResponse response = replayService.updateReplay(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getReplayId());
        assertEquals("https://www.youtube.com/watch?v=xyz789", response.getFileUrl());
        verify(replayRepository, times(1)).save(any(Replay.class));
    }

    @Test
    @DisplayName("리플레이 삭제 성공 테스트")
    void deleteReplaySuccess() {
        // Given
        when(replayRepository.existsById(1L)).thenReturn(true);
        doNothing().when(replayRepository).deleteById(1L);

        // When
        replayService.deleteReplay(1L);

        // Then
        verify(replayRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 리플레이 삭제 실패 테스트")
    void deleteReplayNotFound() {
        // Given
        when(replayRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> replayService.deleteReplay(999L));
        verify(replayRepository, never()).deleteById(any());
    }
}
