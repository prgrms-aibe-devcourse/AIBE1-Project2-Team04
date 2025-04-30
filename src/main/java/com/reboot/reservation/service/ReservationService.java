package com.reboot.reservation.service;

import com.reboot.reservation.dto.ReservationResponse;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationResponse getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("해당 예약이 존재하지 않습니다: " + reservationId));

        return convertToResponseDto(reservation);
    }

    public List<ReservationResponse> getReservationsByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberMemberId(memberId);

        return reservations.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getReservationsByInstructorId(Long instructorId) {
        List<Reservation> reservations = reservationRepository.findByInstructorInstructorId(instructorId);

        return reservations.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 내부적으로 사용할 변환 메소드
    private ReservationResponse convertToResponseDto(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .memberId(reservation.getMember().getMemberId())
                .memberName(reservation.getMember().getName())
                .instructorId(reservation.getInstructor().getInstructorId())
                .instructorName(reservation.getInstructor().getMember().getName())
                .lectureId(reservation.getLecture().getLectureId())
                .lectureTitle(reservation.getLecture().getTitle())
                .date(reservation.getDate())
                .status(reservation.getStatus())
                .build();
    }
}
