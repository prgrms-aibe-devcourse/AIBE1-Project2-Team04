package com.reboot.reservation.controller;

import com.reboot.lecture.entity.Lecture;
import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.service.LectureService;
import com.reboot.auth.service.MemberService;
import com.reboot.reservation.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final LectureService lectureService;
    private final MemberService memberService;
    private final ReplayService replayService;
    /**
     * 예약 폼 페이지 진입
     */
    @GetMapping("/new")
    public String reservationForm(@RequestParam Long lectureId, @RequestParam Long memberId, Model model) {
        Member member = memberService.getMember(memberId);
        Lecture lecture = lectureService.getLecture(lectureId);
        model.addAttribute("member", member);
        model.addAttribute("lecture", lecture);
        model.addAttribute("reservationRequestDto", new ReservationRequestDto(
                member.getMemberId(),
                lecture.getInstructor().getInstructorId(),
                lecture.getId(),
                null,
                null,
                null
        ));

        // 기존 예약이 있는 경우 해당 예약의 리플레이 목록도 조회
        try {
            List<ReservationResponseDto> existingReservations = reservationService.getReservationsByMemberAndLecture(memberId, lectureId);
            if (!existingReservations.isEmpty()) {
                ReservationResponseDto existingReservation = existingReservations.get(0);
                List<ReplayResponse> existingReplays = replayService.getReplaysByReservationId(existingReservation.getReservationId());
                model.addAttribute("existingReplays", existingReplays);
                model.addAttribute("existingReservation", existingReservation);
            }
        } catch (Exception e) {
            // 기존 예약 정보 조회 실패 시 무시
        }

        return "reservation/reservationForm";
    }

    /**
     * 예약 생성 요청 처리
     */
    @PostMapping
    public String createReservation(@ModelAttribute ReservationRequestDto requestDto,
                                    @RequestParam(required = false) List<String> replayUrls,
                                    RedirectAttributes redirectAttributes) {
        try {
            // 예약 생성
            ReservationResponseDto responseDto = reservationService.createReservation(requestDto);

            // 리플레이 URLs가 있으면 처리
            if (replayUrls != null && !replayUrls.isEmpty()) {
                for (String url : replayUrls) {
                    ReplayRequest replayRequest = new ReplayRequest();
                    replayRequest.setReservationId(responseDto.getReservationId());
                    replayRequest.setFileUrl(url);
                    replayService.saveReplay(replayRequest);
                }
            }

            // 리다이렉트 시 예약 ID 전달
            return "redirect:/reservation/" + responseDto.getReservationId();
        } catch (Exception e) {
            // 오류 처리
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservation/new?lectureId=" + requestDto.getLectureId() + "&memberId=" + requestDto.getMemberId();
        }
    }

    /**
     * 예약 취소 폼 진입
     * - GET /reservation/{id}/cancel
     */
    @GetMapping("/{id}/cancel")
    public String cancelForm(@PathVariable Long id, Model model) {
        model.addAttribute("reservationCancelDto", new ReservationCancelDto(id, null));
        return "reservation/reservationCancel";
    }

    @GetMapping("/{reservationId}")
    public String viewReservation(@PathVariable Long reservationId, Model model) {
        try {
            // 예약 정보 로드
            ReservationResponseDto reservation = reservationService.getReservation(reservationId);
            model.addAttribute("reservation", reservation);

            // 예약에 연결된 리플레이 로드 (중요!)
            List<ReplayResponse> replays = replayService.getReplaysByReservationId(reservationId);
            if (!replays.isEmpty()) {
                model.addAttribute("replay", replays.get(0)); // 첫 번째 리플레이 표시
                model.addAttribute("allReplays", replays); // 모든 리플레이 목록도 추가
            }

            return "reservation/view";
        } catch (EntityNotFoundException e) {
            return "redirect:/error";
        }
    }

    /**
     * 예약 취소 요청 처리
     * - POST /reservation/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public String cancelReservation(@ModelAttribute ReservationCancelDto cancelDto, Model model) {
        ReservationResponseDto reservation = reservationService.cancelReservation(cancelDto);
        model.addAttribute("reservation", reservation);
        return "reservation/reservationResult";
    }

    /**
     * 예약 상세 조회
     */
    @GetMapping("/{id}")
    public String getReservation(@PathVariable Long id, Model model) {
        ReservationResponseDto reservation = reservationService.getReservation(id);
        model.addAttribute("reservation", reservation);

        // 리플레이 정보 추가 (있는 경우)
        if (reservation.getReplayId() != null) {
            try {
                ReplayResponse replay = replayService.getReplay(reservation.getReplayId());
                model.addAttribute("replay", replay);
            } catch (Exception e) {
                // 리플레이 정보 조회 실패시 무시
            }
        }

        return "reservation/reservationDetail";
    }

    /**
     * 회원별 예약 목록 조회
     * - GET /reservation/member/{memberId}
     */
    @GetMapping("/member/{memberId}")
    public String getReservationsByMember(@PathVariable Long memberId, Model model) {
        model.addAttribute("reservations", reservationService.getReservationsByMember(memberId));
        return "reservation/reservationList";
    }
}