package com.reboot.reservation.service;

import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.entity.Lecture;
import com.reboot.auth.entity.Member;
import com.reboot.reservation.entity.Reservation;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.reservation.repository.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 예약(Reservation) 도메인의 비즈니스 로직을 담당하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final InstructorRepository instructorRepository;
    private final LectureRepository lectureRepository;

    /**
     * 새로운 예약을 생성한다.
     * @param dto 예약 요청 DTO (회원ID, 강사ID, 강의ID 등)
     * @return 생성된 예약의 응답 DTO
     */
    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto dto) {

        if(dto.getMemberId() == null) throw new IllegalArgumentException("memberId is null");
        if(dto.getLectureId() == null) throw new IllegalArgumentException("lectureId is null");
        if(dto.getInstructorId() == null) throw new IllegalArgumentException("instructorId is null");

        // 회원, 강사, 강의 엔티티를 각각 조회 (존재하지 않으면 예외 발생)
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Instructor instructor = instructorRepository.findById(dto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("강사를 찾을 수 없습니다."));

        Lecture lecture = lectureRepository.findById(dto.getLectureId())
                .orElseThrow(() -> new RuntimeException("강의를 찾을 수 없습니다."));

        // 예약 엔티티 생성 및 저장
        Reservation reservation = Reservation.builder()
                .member(member)
                .instructor(instructor)
                .lecture(lecture)
                .requestDetail(dto.getRequestDetail())
                .scheduleDate(dto.getScheduleDate())
                .date(LocalDateTime.now())
                .status("예약완료")
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 엔티티를 DTO로 변환하여 반환
        return convertToDto(savedReservation);
    }

    /**
     * 예약 ID로 예약 상세 조회
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        return convertToDto(reservation);
    }

    /**
     * 회원 ID로 해당 회원의 모든 예약 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByMember(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberMemberId(memberId);

        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 예약 취소 처리
     * @param cancelDto 취소 요청 DTO (예약ID, 취소사유)
     * @return 취소된 예약의 응답 DTO
     */
    @Transactional
    public ReservationResponseDto cancelReservation(ReservationCancelDto cancelDto) {
        Reservation reservation = reservationRepository.findById(cancelDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        reservation.setStatus("취소");
        reservation.setCancelReason(cancelDto.getCancelReason());
        Reservation savedReservation = reservationRepository.save(reservation);

        return convertToDto(savedReservation);
    }

    /**
     * 예약 ID로 예약 취소 처리 (상태만 '취소'로 변경)
     * @deprecated 취소 사유를 받는 메소드로 대체됨 {@link #cancelReservation(ReservationCancelDto)}
     */
    @Transactional
    @Deprecated
    public ReservationResponseDto cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        reservation.setStatus("취소");
        Reservation savedReservation = reservationRepository.save(reservation);

        return convertToDto(savedReservation);
    }

    /**
     * Reservation 엔티티를 ReservationResponseDto로 변환
     */
    private ReservationResponseDto convertToDto(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getReservationId())
                .memberId(reservation.getMember().getMemberId())
                .memberName(reservation.getMember().getName())
                .instructorId(reservation.getInstructor().getInstructorId())
                .instructorName(reservation.getInstructor().getMember().getName())
                .lectureId(reservation.getLecture().getId())
                .lectureTitle(reservation.getLecture().getInfo().getTitle())
                .date(reservation.getDate())
                .status(reservation.getStatus())
                .requestDetail(reservation.getRequestDetail())
                .scheduleDate(reservation.getScheduleDate())
                .cancelReason(reservation.getCancelReason())
                .build();
    }
}