package com.reboot.replay;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.entity.Replay;
import com.reboot.replay.repository.ReplayRepository;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Replay 모듈만 독립적으로 테스트하는 통합 테스트 클래스
 * 모킹을 사용하여 다른 모듈의 의존성을 처리합니다.
 */

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // 이 줄 추가
@ActiveProfiles("test")
public class ReplayModuleTest {

    private ReplayService replayService;

    @Mock
    private ReplayRepository replayRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private Reservation testReservation;
    private Replay testReplay;
    private ReplayRequest testRequest;

    @BeforeEach
    void setUp() {
        // ReplayService 초기화 (생성자 주입 방식 사용)
        replayService = new ReplayService(replayRepository, reservationRepository);

        // 테스트 데이터 초기화
        reset(replayRepository);
        reset(reservationRepository);

        // 테스트용 예약 객체
        testReservation = new Reservation();
        testReservation.setReservationId(1L);

        // 테스트용 리플레이 객체
        testReplay = Replay.builder()
                .replayId(1L)
                .reservation(testReservation)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .date(LocalDateTime.now())
                .build();

        // 테스트용 요청 객체
        testRequest = ReplayRequest.builder()
                .reservationId(1L)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .build();

        // ✅ Mock 동작 설정 추가
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(replayRepository.save(any(Replay.class))).thenReturn(testReplay);
    }

    @Test
    @DisplayName("리플레이 모듈 독립 테스트 - 리플레이 저장")
    void saveReplayTest() {
        // 테스트 실행
        ReplayResponse response = replayService.saveReplay(testRequest);

        // 검증
        assertNotNull(response);
        assertEquals(1L, response.getReplayId());
        assertEquals("https://www.youtube.com/watch?v=abc123", response.getFileUrl());

        // 서비스가 저장소 메서드를 호출했는지 검증
        verify(reservationRepository).findById(1L);
        verify(replayRepository).save(any(Replay.class));
    }

    @Test
    @DisplayName("유튜브 영상 ID 추출 테스트")
    void youtubeVideoIdExtractionTest() {
        // 다양한 형태의 유튜브 URL에서 영상 ID 추출 테스트
        ReplayResponse response1 = ReplayResponse.builder()
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .build();

        ReplayResponse response2 = ReplayResponse.builder()
                .fileUrl("https://youtu.be/def456")
                .build();

        ReplayResponse response3 = ReplayResponse.builder()
                .fileUrl("https://www.youtube.com/watch?v=ghi789&feature=youtu.be")
                .build();

        // 검증
        assertEquals("abc123", response1.getYoutubeVideoId());
        assertEquals("def456", response2.getYoutubeVideoId());
        assertEquals("ghi789", response3.getYoutubeVideoId());
    }

    @Test
    @DisplayName("유튜브 임베드 URL 생성 테스트")
    void youtubeEmbedUrlGenerationTest() {
        // 유튜브 임베드 URL 생성 테스트
        ReplayResponse response = ReplayResponse.builder()
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .build();

        // 검증
        assertEquals("https://www.youtube.com/embed/abc123", response.getYoutubeEmbedUrl());
    }
}