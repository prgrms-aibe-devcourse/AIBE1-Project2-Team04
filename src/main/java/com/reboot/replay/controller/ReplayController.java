package com.reboot.replay.controller;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.lecture.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    @PostMapping("/{replayId}/delete")
    public String deleteReplay(@PathVariable Long replayId,
                               @RequestParam Long reservationId,
                               RedirectAttributes redirectAttributes) {
        try {
            replayService.deleteReplay(replayId);
            redirectAttributes.addFlashAttribute("message", "영상이 성공적으로 삭제되었습니다.");
            return "redirect:/replays/reservation/" + reservationId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/replays/" + replayId;
        }
    }
}