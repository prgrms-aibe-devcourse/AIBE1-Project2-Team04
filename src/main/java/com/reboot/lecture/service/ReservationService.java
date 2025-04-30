package com.reboot.lecture.service;

import com.reboot.lecture.entity.Reservation;
import com.reboot.lecture.dto.ReservationResponseDto;
import com.reboot.lecture.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationResponseDto getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("해당 예약이 존재하지 않습니다: " + reservationId));

        return convertToResponseDto(reservation);
    }

    public List<ReservationResponseDto> getReservationsByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberMemberId(memberId);

        return reservations.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ReservationResponseDto> getReservationsByInstructorId(Long instructorId) {
        List<Reservation> reservations = reservationRepository.findByInstructorInstructorId(instructorId);

        return reservations.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 내부적으로 사용할 변환 메소드
    private ReservationResponseDto convertToResponseDto(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getReservationId())
                .studentId(reservation.getMember().getMemberId())
                .studentName(reservation.getMember().getName())
                .instructorId(reservation.getInstructor().getInstructorId())
                .instructorName(reservation.getInstructor().getMember().getName())
                .lectureId(reservation.getLecture().getLectureId())
                .lectureTitle(reservation.getLecture().getTitle())
                .date(reservation.getDate())
                .status(reservation.getStatus())
                .build();
    }
}
