package com.reboot.replay.controller;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReplayControllerTest {

    @Mock
    private ReplayService replayService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReplayController replayController;

    private MockMvc mockMvc;
    private ReplayResponse testReplayResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(replayController).build();

        // 테스트용 응답 객체 생성
        testReplayResponse = ReplayResponse.builder()
                .replayId(1L)
                .reservationId(1L)
                .fileUrl("https://www.youtube.com/watch?v=abc123")
                .date(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("리플레이 업로드 폼 페이지 테스트")
    void showUploadForm() throws Exception {
        mockMvc.perform(get("/replays/upload"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("replayRequest"))
                .andExpect(view().name("replay/upload-form"));
    }

    @Test
    @DisplayName("예약 ID가 있는 리플레이 업로드 폼 페이지 테스트")
    void showUploadFormWithReservationId() throws Exception {
        // 예약 정보 모킹
        when(reservationService.getReservation(eq(1L))).thenReturn(null);

        mockMvc.perform(get("/replays/upload").param("reservationId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("replay/upload-form"))
                .andExpect(model().attributeExists("replayRequest"));
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID로 업로드 폼 페이지 접근 테스트")
    void showUploadFormWithNonExistentReservationId() throws Exception {
        // 예약이 존재하지 않는 경우 예외 발생
        when(reservationService.getReservation(999L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/replays/upload").param("reservationId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));
    }

    @Test
    @DisplayName("리플레이 저장 성공 테스트")
    void saveReplaySuccess() throws Exception {
        // 리플레이 저장 서비스 모킹
        when(replayService.saveReplay(any(ReplayRequest.class))).thenReturn(testReplayResponse);

        mockMvc.perform(post("/replays")
                        .param("reservationId", "1")
                        .param("fileUrl", "https://www.youtube.com/watch?v=abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/replays/1"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("리플레이 저장 실패 테스트")
    void saveReplayFail() throws Exception {
        // 예외 발생 모킹
        when(replayService.saveReplay(any(ReplayRequest.class))).thenThrow(new RuntimeException("저장 실패"));

        mockMvc.perform(post("/replays")
                        .param("reservationId", "1")
                        .param("fileUrl", "https://www.youtube.com/watch?v=abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/replays/upload?reservationId=1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("리플레이 상세 조회 성공 테스트")
    void viewReplaySuccess() throws Exception {
        // 리플레이 조회 서비스 모킹
        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);

        mockMvc.perform(get("/replays/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("replay"))
                .andExpect(view().name("replay/view"));
    }

    @Test
    @DisplayName("존재하지 않는 리플레이 조회 테스트")
    void viewReplayNotFound() throws Exception {
        // 예외 발생 모킹
        when(replayService.getReplay(999L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/replays/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));
    }

    @Test
    @DisplayName("예약별 리플레이 목록 조회 테스트")
    void listReplaysByReservation() throws Exception {
        // 리플레이 목록 조회 서비스 모킹
        List<ReplayResponse> replayList = Arrays.asList(
                testReplayResponse,
                ReplayResponse.builder()
                        .replayId(2L)
                        .reservationId(1L)
                        .fileUrl("https://www.youtube.com/watch?v=def456")
                        .date(LocalDateTime.now())
                        .build()
        );
        when(replayService.getReplaysByReservationId(1L)).thenReturn(replayList);
        when(reservationService.getReservation(eq(1L))).thenReturn(null);

        mockMvc.perform(get("/replays/reservation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("replay/list"))
                .andExpect(model().attributeExists("replays"));
    }

    @Test
    @DisplayName("존재하지 않는 예약의 리플레이 목록 조회 테스트")
    void listReplaysByNonExistentReservation() throws Exception {
        // 리플레이 목록 조회 서비스 모킹
        List<ReplayResponse> emptyList = Arrays.asList();
        when(replayService.getReplaysByReservationId(999L)).thenReturn(emptyList);
        when(reservationService.getReservation(999L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/replays/reservation/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));
    }

    @Test
    @DisplayName("리플레이 수정 폼 페이지 테스트")
    void showEditForm() throws Exception {
        // 리플레이 조회 서비스 모킹
        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);
        when(reservationService.getReservation(eq(1L))).thenReturn(null);

        mockMvc.perform(get("/replays/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("replay/edit-form"))  // 뷰 이름 수정
                .andExpect(model().attributeExists("replayRequest"))
                .andExpect(model().attributeExists("replayId"));
    }

    @Test
    @DisplayName("존재하지 않는 리플레이 수정 폼 접근 테스트")
    void showEditFormNonExistentReplay() throws Exception {
        // 예외 발생 모킹
        when(replayService.getReplay(999L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/replays/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));
    }

    @Test
    @DisplayName("리플레이 수정 성공 테스트")
    void updateReplaySuccess() throws Exception {
        // 리플레이 조회 및 수정 서비스 모킹
        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);
        when(replayService.updateReplay(eq(1L), any(ReplayRequest.class))).thenReturn(testReplayResponse);

        mockMvc.perform(post("/replays/1/update")
                        .param("fileUrl", "https://www.youtube.com/watch?v=xyz789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/replays/1"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("리플레이 수정 실패 테스트")
    void updateReplayFail() throws Exception {
        // 리플레이 조회 서비스 모킹과 수정 실패 모킹
        when(replayService.getReplay(1L)).thenReturn(testReplayResponse);
        when(replayService.updateReplay(eq(1L), any(ReplayRequest.class))).thenThrow(new RuntimeException("수정 실패"));

        mockMvc.perform(post("/replays/1/update")
                        .param("fileUrl", "https://www.youtube.com/watch?v=xyz789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/replays/1/edit"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("리플레이 삭제 성공 테스트")
    void deleteReplaySuccess() throws Exception {
        // 리플레이 삭제 서비스 모킹
        doNothing().when(replayService).deleteReplay(1L);

        mockMvc.perform(post("/replays/1/delete")
                        .param("reservationId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/replays/reservation/1"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("리플레이 삭제 실패 테스트")
    void deleteReplayFail() throws Exception {
        // 리플레이 삭제 실패 모킹
        doThrow(new RuntimeException("삭제 실패")).when(replayService).deleteReplay(1L);

        mockMvc.perform(post("/replays/1/delete")
                        .param("reservationId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/replays/1"))
                .andExpect(flash().attributeExists("error"));
    }
}