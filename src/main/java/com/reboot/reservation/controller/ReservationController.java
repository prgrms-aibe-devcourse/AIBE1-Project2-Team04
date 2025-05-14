package com.reboot.reservation.controller;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.service.LectureService;
import com.reboot.auth.service.MemberService;
import com.reboot.reservation.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
@Slf4j
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
            log.info("예약 생성 요청: {}", requestDto);
            // 유효성 검사 추가
            if (requestDto.getLectureId() == null) {
                throw new IllegalArgumentException("강의 ID가 필요합니다.");
            }
            if (requestDto.getMemberId() == null) {
                throw new IllegalArgumentException("회원 ID가 필요합니다.");
            }
            if (requestDto.getInstructorId() == null) {
                throw new IllegalArgumentException("강사 ID가 필요합니다.");
            }

            // 예약 생성
            ReservationResponseDto responseDto = reservationService.createReservation(requestDto);
            log.info("예약 생성 완료: 예약 ID = {}", responseDto.getReservationId());

            // 리플레이 URLs가 있으면 처리
            if (replayUrls != null && !replayUrls.isEmpty()) {
                log.info("리플레이 URL 처리: {} 개", replayUrls.size());
                for (String url : replayUrls) {
                    if (url != null && !url.trim().isEmpty()) {
                        ReplayRequest replayRequest = new ReplayRequest();
                        replayRequest.setReservationId(responseDto.getReservationId());
                        replayRequest.setFileUrl(url);
                        replayService.saveReplay(replayRequest);
                    }
                }
            }

            // 리다이렉트 시 플래그 설정 (캐시 갱신용)
            redirectAttributes.addFlashAttribute("refresh", true);

            // 예약 ID가 null인지 확인
            if (responseDto.getReservationId() == null) {
                log.error("생성된 예약의 ID가 null입니다");
                return "redirect:/error";
            }

            // 리다이렉트 시 예약 ID 전달
            return "redirect:/reservation/" + responseDto.getReservationId();
        } catch (Exception e) {
            // 오류 로깅
            log.error("예약 생성 중 오류 발생: {}", e.getMessage(), e);

            // 오류 처리
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            // lectureId와 memberId가 null이 아닌지 확인
            if (requestDto.getLectureId() != null && requestDto.getMemberId() != null) {
                return "redirect:/reservation/new?lectureId=" + requestDto.getLectureId() + "&memberId=" + requestDto.getMemberId();
            } else {
                return "redirect:/error";
            }
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

    @GetMapping("/{id}/view")
    public String viewReservation(@PathVariable Long id, Model model) {
        try {
            // 예약 정보 로드
            ReservationResponseDto reservation = reservationService.getReservation(id);
            model.addAttribute("reservation", reservation);

            // 예약에 연결된 리플레이 로드 (중요!)
            List<ReplayResponse> replays = replayService.getReplaysByReservationId(id);
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
    public String cancelReservation(@ModelAttribute ReservationCancelDto cancelDto, RedirectAttributes redirectAttributes) {
        try {
            ReservationResponseDto reservation = reservationService.cancelReservation(cancelDto);
            redirectAttributes.addFlashAttribute("message", "예약이 성공적으로 취소되었습니다.");
            redirectAttributes.addFlashAttribute("refresh", true); // 캐시 갱신 플래그
            return "redirect:/reservation/" + reservation.getReservationId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/reservation/" + cancelDto.getReservationId();
        }
    }

    /**
     * 예약 상세 조회
     */
    @GetMapping("/{id}")
    public String getReservation(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 예약 정보 조회
            ReservationResponseDto reservation = reservationService.getReservation(id);

            // 예약에 연결된 모든 리플레이 조회하여 reservation 객체에 설정
            if (reservation.getReplays() == null || reservation.getReplays().isEmpty()) {
                List<ReplayResponse> replays = replayService.getReplaysByReservationId(id);
                reservation.setReplays(replays);

                // 기존 호환성을 위해 첫 번째 리플레이 정보도 설정
                if (!replays.isEmpty()) {
                    ReplayResponse firstReplay = replays.get(0);
                    reservation.setReplayId(firstReplay.getReplayId());
                    reservation.setReplayUrl(firstReplay.getFileUrl());
                }
            }

            model.addAttribute("reservation", reservation);
            model.addAttribute("reservationId", reservation.getReservationId());

            return "reservation/reservationResult";
        } catch (EntityNotFoundException e) {
            log.error("예약 정보를 찾을 수 없음: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "해당 예약 정보를 찾을 수 없습니다.");
            return "redirect:/error";
        } catch (Exception e) {
            log.error("예약 조회 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "예약 정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/error";
        }
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

    /**
     * AJAX 요청 처리: 예약 생성
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createReservationApi(@RequestBody ReservationRequestDto requestDto,
                                                  @RequestParam(required = false) List<String> replayUrls) {
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

            // 성공 응답
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "예약이 성공적으로 생성되었습니다.");
            response.put("reservationId", responseDto.getReservationId());
            response.put("redirectUrl", "/reservation/" + responseDto.getReservationId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}