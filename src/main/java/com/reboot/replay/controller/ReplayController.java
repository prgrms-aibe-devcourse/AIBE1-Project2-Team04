package com.reboot.replay.controller;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/replays")
@RequiredArgsConstructor
public class ReplayController {

    private final ReplayService replayService;
    private final ReservationService reservationService;

    // 리플레이 업로드 폼 페이지
    @GetMapping("/upload")
    public String showUploadForm(@RequestParam(required = false) Long reservationId, Model model) {
        ReplayRequest request = new ReplayRequest();
        if (reservationId != null) {
            request.setReservationId(reservationId);
        }
        model.addAttribute("replayRequest", request);

        // 예약 정보가 있으면 해당 정보도 모델에 추가
        if (reservationId != null) {
            try {
                model.addAttribute("reservation", reservationService.getReservation(reservationId));
            } catch (EntityNotFoundException e) {
                // 예약이 존재하지 않는 경우의 처리
                return "redirect:/error";
            }
        }

        return "replay/upload-form";
    }

    // ReplayController에 추가
    @GetMapping("/reservation/add/{reservationId}")
    public String showAddReplayForm(@PathVariable Long reservationId, Model model) {
        try {
            // 예약 정보 조회
            ReservationResponseDto reservation = reservationService.getReservation(reservationId);
            model.addAttribute("reservation", reservation);

            // 이미 리플레이가 있는지 확인
            List<ReplayResponse> existingReplays = replayService.getReplaysByReservationId(reservationId);
            if (!existingReplays.isEmpty()) {
                // 리플레이가 이미 있는 경우, 템플릿에서 처리하기 위해 모델에 추가
                model.addAttribute("existingReplays", existingReplays);
            }

            // 새 리플레이 요청 객체 생성
            ReplayRequest replayRequest = new ReplayRequest();
            replayRequest.setReservationId(reservationId);
            model.addAttribute("replayRequest", replayRequest);

            return "replay/upload-form";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    @GetMapping
    public String listAllReplays(Model model) {
        List<ReplayResponse> replays = replayService.getAllReplays();
        model.addAttribute("replays", replays);
        return "replay/list-all"; // 모든 리플레이 목록용 템플릿
    }

    // 리플레이 저장 처리
    @PostMapping
    public String saveReplay(@ModelAttribute("replayRequest") ReplayRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            ReplayResponse response = replayService.saveReplay(request);
            redirectAttributes.addFlashAttribute("message", "영상이 성공적으로 업로드되었습니다.");
            return "redirect:/replays/" + response.getReplayId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/replays/upload?reservationId=" + request.getReservationId();
        }
    }

    // 리플레이 상세 조회
    @GetMapping("/{replayId}")
    public String viewReplay(@PathVariable Long replayId, Model model) {
        try {
            ReplayResponse replay = replayService.getReplay(replayId);
            model.addAttribute("replay", replay);
            return "replay/view";
        } catch (EntityNotFoundException e) {
            return "redirect:/error";
        }
    }

    // 예약별 리플레이 목록 조회
    @GetMapping("/reservation/{reservationId}")
    public String listReplaysByReservation(@PathVariable Long reservationId, Model model) {
        List<ReplayResponse> replays = replayService.getReplaysByReservationId(reservationId);
        model.addAttribute("replays", replays);

        try {
            model.addAttribute("reservation", reservationService.getReservation(reservationId));
        } catch (EntityNotFoundException e) {
            return "redirect:/error";
        }

        return "replay/list";
    }

    // 리플레이 수정 폼 페이지
    @GetMapping("/{replayId}/edit")
    public String showEditForm(@PathVariable Long replayId, Model model) {
        try {
            ReplayResponse replay = replayService.getReplay(replayId);

            // ReplayResponse를 ReplayRequest로 변환하여 폼에 사용
            ReplayRequest request = ReplayRequest.builder()
                    .reservationId(replay.getReservationId())
                    .fileUrl(replay.getFileUrl())
                    .build();

            model.addAttribute("replayRequest", request);
            model.addAttribute("replayId", replayId);

            // 예약 정보도 모델에 추가
            try {
                model.addAttribute("reservation", reservationService.getReservation(replay.getReservationId()));
            } catch (EntityNotFoundException e) {
                // 예약 정보가 없는 경우 무시
            }

            return "replay/edit-form";
        } catch (EntityNotFoundException e) {
            return "redirect:/error";
        }
    }

    // 리플레이 수정 처리 - 수정됨 (예약 정보 변경 로직 제거)
    @PostMapping("/{replayId}/update")
    public String updateReplay(@PathVariable Long replayId,
                               @ModelAttribute("replayRequest") ReplayRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            // 현재 replay의 reservation_id를 유지하기 위해 먼저 조회
            ReplayResponse currentReplay = replayService.getReplay(replayId);

            // request에 현재 예약 ID 설정 (폼에서 넘어온 값 무시)
            request.setReservationId(currentReplay.getReservationId());

            // 업데이트 수행
            ReplayResponse response = replayService.updateReplay(replayId, request);
            redirectAttributes.addFlashAttribute("message", "영상이 성공적으로 수정되었습니다.");
            return "redirect:/replays/" + response.getReplayId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/replays/" + replayId + "/edit";
        }
    }

    // 리플레이 삭제 처리
// 리플레이 삭제 API
    @DeleteMapping("/{replayId}")
    public ResponseEntity<?> deleteReplay(@PathVariable Long replayId) {
        try {
            // 리플레이 삭제 로직
            replayService.deleteReplay(replayId);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "리플레이가 삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "리플레이 삭제에 실패했습니다: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}