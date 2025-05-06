package com.reboot.reservation.controller;

import com.reboot.lecture.entity.Lecture;
import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.service.LectureService;
import com.reboot.auth.service.MemberService;
import com.reboot.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 예약(Reservation) 관련 HTTP 요청을 처리하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final LectureService lectureService;
    private final MemberService memberService;

    /**
     * 예약 폼 페이지 진입
     * - GET /reservation/new?lectureId=xxx
     * - 강의 정보를 조회하여 예약 폼에 전달
     */
    @GetMapping("/new")
    public String reservationForm(@RequestParam String lectureId, @RequestParam Long memberId, Model model) {
        Member member = memberService.getMember(memberId);
        Lecture lecture = lectureService.getLecture(lectureId);
        model.addAttribute("member", member);
        model.addAttribute("lecture", lecture);
        model.addAttribute("reservationRequestDto", new ReservationRequestDto(
                member.getMemberId(),
                lecture.getInstructor().getInstructorId(),
                lecture.getId(),
                null,
                null
        ));
        return "reservation/reservationForm";
    }

    /**
     * 예약 생성 요청 처리
     * - POST /reservation
     * - 폼에서 입력받은 데이터를 바탕으로 예약 생성
     */
    @PostMapping
    public String createReservation(@ModelAttribute ReservationRequestDto dto, Model model) {
        ReservationResponseDto reservation = reservationService.createReservation(dto);
        model.addAttribute("reservation", reservation);
        return "reservation/reservationResult";
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
     * - GET /reservation/{id}
     */
    @GetMapping("/{id}")
    public String getReservation(@PathVariable Long id, Model model) {
        ReservationResponseDto reservation = reservationService.getReservation(id);
        model.addAttribute("reservation", reservation);
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