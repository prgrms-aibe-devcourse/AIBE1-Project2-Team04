package com.reboot.reservation.service;

import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
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

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final InstructorRepository instructorRepository;
    private final LectureRepository lectureRepository;
    private final ReplayService replayService; // ReplayService 주입

    /**
     * 새로운 예약을 생성한다. 리플레이 URL이 있으면 함께 생성.
     * @param dto 예약 요청 DTO (회원ID, 강사ID, 강의ID 등)
     * @return 생성된 예약의 응답 DTO
     */
    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto dto) {
        // 기존 검증 로직
        if(dto.getMemberId() == null) throw new IllegalArgumentException("memberId is null");
        if(dto.getLectureId() == null) throw new IllegalArgumentException("lectureId is null");
        if(dto.getInstructorId() == null) throw new IllegalArgumentException("instructorId is null");

        // 회원, 강사, 강의 엔티티를 각각 조회
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

        // 리플레이 URL이 있으면 리플레이 생성
        ReservationResponseDto responseDto = convertToDto(savedReservation);

        if (dto.getYoutubeUrl() != null && !dto.getYoutubeUrl().trim().isEmpty()) {
            try {
                ReplayRequest replayRequest = new ReplayRequest();
                replayRequest.setReservationId(savedReservation.getReservationId());
                replayRequest.setFileUrl(dto.getYoutubeUrl());

                ReplayResponse replayResponse = replayService.saveReplay(replayRequest);
                responseDto.setReplayId(replayResponse.getReplayId());
                responseDto.setReplayUrl(replayResponse.getFileUrl());
            } catch (Exception e) {
                // 리플레이 생성 실패해도 예약은 성공한 것으로 처리
                // 로깅 추가
                System.out.println("리플레이 생성 실패: " + e.getMessage());
            }
        }

        return responseDto;
    }

    /**
     * 회원 ID와 강의 ID로 해당하는 예약 목록 조회
     * @param memberId 회원 ID
     * @param lectureId 강의 ID
     * @return 조회된 예약 목록
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByMemberAndLecture(Long memberId, Long lectureId) {
        List<Reservation> reservations = reservationRepository.findByMemberMemberIdAndLectureId(memberId, lectureId);

        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 예약 ID로 예약 상세 조회 (리플레이 정보 포함)
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        ReservationResponseDto dto = convertToDto(reservation);

        // 리플레이 정보 조회
        try {
            List<ReplayResponse> replays = replayService.getReplaysByReservationId(reservationId);
            if (!replays.isEmpty()) {
                ReplayResponse replay = replays.get(0);  // 첫 번째 리플레이 정보만 사용
                dto.setReplayId(replay.getReplayId());
                dto.setReplayUrl(replay.getFileUrl());
            }
        } catch (Exception e) {
            // 리플레이 정보 조회 실패해도 예약 정보는 반환
            System.out.println("리플레이 정보 조회 실패: " + e.getMessage());
        }

        return dto;
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

        if (reservation.getReplay() != null) {
            replayService.deleteReplay(reservation.getReplay().getReplayId());
        }

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
        // 새 DTO 객체 생성
        ReservationResponseDto dto = new ReservationResponseDto();

        // 기본 필드 설정
        dto.setReservationId(reservation.getReservationId());
        dto.setMemberId(reservation.getMember().getMemberId());
        dto.setMemberName(reservation.getMember().getName());
        dto.setInstructorId(reservation.getInstructor().getInstructorId());
        dto.setInstructorName(reservation.getInstructor().getMember().getName());
        dto.setLectureId(reservation.getLecture().getId());
        dto.setDate(reservation.getDate());
        dto.setStatus(reservation.getStatus());
        dto.setRequestDetail(reservation.getRequestDetail());
        dto.setScheduleDate(reservation.getScheduleDate());
        dto.setCancelReason(reservation.getCancelReason());

        // null 체크 추가
        if (reservation.getLecture() != null &&
                reservation.getLecture().getInfo() != null) {
            dto.setLectureTitle(reservation.getLecture().getInfo().getTitle());
        } else {
            dto.setLectureTitle(""); // 정보가 없을 경우 빈 문자열로 설정
        }

        // 리플레이 정보 추가 (엔티티 관계를 통해 직접 조회)
        if (reservation.getReplay() != null) {
            dto.setReplayId(reservation.getReplay().getReplayId());
            dto.setReplayUrl(reservation.getReplay().getFileUrl());
        }

        return dto;
    }
}