package com.reboot.replay.controller;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/replays")
@RequiredArgsConstructor
public class ReplayRestController {

    private final ReplayService replayService;

    /**
     * 리플레이 생성 API
     */
    @PostMapping
    public ResponseEntity<?> createReplay(@RequestBody ReplayRequest request) {
        try {
            ReplayResponse response = replayService.saveReplay(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 예약별 리플레이 목록 조회 API
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<?> getReplaysByReservation(@PathVariable Long reservationId) {
        try {
            List<ReplayResponse> replays = replayService.getReplaysByReservationId(reservationId);
            return ResponseEntity.ok(replays);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 리플레이 수정 API
     */
    @PutMapping("/{replayId}")
    public ResponseEntity<?> updateReplay(@PathVariable Long replayId, @RequestBody ReplayRequest request) {
        try {
            // 현재 replay의 reservation_id를 유지하기 위해 먼저 조회
            ReplayResponse currentReplay = replayService.getReplay(replayId);
            
            // request에 현재 예약 ID 설정 (요청에서 넘어온 값 무시)
            request.setReservationId(currentReplay.getReservationId());
            
            ReplayResponse response = replayService.updateReplay(replayId, request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "리플레이를 찾을 수 없습니다: " + replayId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 리플레이 삭제 API
     */
    @DeleteMapping("/{replayId}")
    public ResponseEntity<?> deleteReplay(@PathVariable Long replayId) {
        try {
            // 삭제 전에 리플레이 정보 가져오기 (예약 ID 확인용)
            ReplayResponse replay = replayService.getReplay(replayId);
            
            // 리플레이 삭제
            replayService.deleteReplay(replayId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "리플레이가 성공적으로 삭제되었습니다.");
            response.put("deletedReplayId", replayId);
            response.put("reservationId", replay.getReservationId());
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
