package com.reboot.reservation.controller;

import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.reservation.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Slf4j
public class ReservationErrorController {

    private final ReservationService reservationService;

    /**
     * 예약 정보 조회 API (디버깅용)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        try {
            ReservationResponseDto reservation = reservationService.getReservation(id);
            return ResponseEntity.ok(reservation);
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "해당 예약을 찾을 수 없습니다: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 오류 처리를 위한 전용 핸들러 (예약 관련)
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        log.error("예약 컨트롤러에서 오류 발생", e);

        ModelAndView mav = new ModelAndView("error/custom-error");
        mav.addObject("errorMessage", "예약 처리 중 오류가 발생했습니다: " + e.getMessage());
        return mav;
    }
}