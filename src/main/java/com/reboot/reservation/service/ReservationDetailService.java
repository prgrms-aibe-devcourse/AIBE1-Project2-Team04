package com.reboot.reservation.service;

import com.reboot.replay.entity.Replay;
import com.reboot.replay.repository.ReplayRepository;
import com.reboot.reservation.dto.ReservationCancelDto;
import com.reboot.reservation.dto.ReservationRequestDto;
import com.reboot.reservation.dto.ReservationResponseDto;
import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.service.ReplayService;
import com.reboot.auth.entity.Instructor;
import com.reboot.lecture.entity.Lecture;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.lecture.repository.LectureRepository;
import com.reboot.auth.repository.MemberRepository;
import com.reboot.reservation.entity.ReservationDetail;
import com.reboot.reservation.repository.ReservationDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationDetailService {
    private final ReservationDetailRepository reservationDetailRepository;
    private final ReplayRepository replayRepository;
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
        ReservationDetail reservationDetail = com.reboot.reservation.entity.ReservationDetail.builder()
                .member(member)
                .instructor(instructor)
                .lecture(lecture)
                .requestDetail(dto.getRequestDetail())
                .scheduleDate(dto.getScheduleDate())
                .date(LocalDateTime.now())
                .status("예약완료")
                .build();

        ReservationDetail savedReservationDetail = reservationDetailRepository.save(reservationDetail);

        // 리플레이 URL이 있으면 리플레이 생성
        ReservationResponseDto responseDto = convertToDto(savedReservationDetail);

        if (dto.getYoutubeUrl() != null && !dto.getYoutubeUrl().trim().isEmpty()) {
            try {
                ReplayRequest replayRequest = new ReplayRequest();
                replayRequest.setReservationDetailId(savedReservationDetail.getReservationDetailId());
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
        List<ReservationDetail> reservationDetails = reservationDetailRepository.findByMember_MemberIdAndLectureId(memberId, lectureId);

        return reservationDetails.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservation(Long reservationDetailId) {
        ReservationDetail reservationDetail = reservationDetailRepository.findByReservationDetailId(reservationDetailId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        ReservationResponseDto dto = convertToDto(reservationDetail);

        // 리플레이 정보 조회
        try {
            List<Replay> replays = replayRepository.findByReservationDetail_ReservationDetailId(reservationDetailId);
            if (!replays.isEmpty()) {
                // 리플레이 목록을 ReplayResponse로 변환하여 DTO에 설정
                List<ReplayResponse> replayResponses = replays.stream()
                        .map(replay -> new ReplayResponse(
                                replay.getReplayId(),
                                replay.getReservationDetail().getReservationDetailId(),
                                replay.getFileUrl(),
                                replay.getDate()
                        ))
                        .collect(Collectors.toList());

                dto.setReplays(replayResponses);

                // 기존 호환성을 위해 첫 번째 리플레이 정보 설정
                if (!replayResponses.isEmpty()) {
                    ReplayResponse firstReplay = replayResponses.get(0);
                    dto.setReplayId(firstReplay.getReplayId());
                    dto.setReplayUrl(firstReplay.getFileUrl());
                }
            }
        } catch (Exception e) {
            // 리플레이 정보 조회 실패해도 예약 정보는 반환
            log.error("리플레이 정보 조회 실패: " + e.getMessage(), e);
        }

        return dto;
    }

    /**
     * 회원 ID로 해당 회원의 모든 예약 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByMember(Long memberId) {
        List<ReservationDetail> reservationDetails = reservationDetailRepository.findByMember_MemberId(memberId);

        return reservationDetails.stream()
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
        ReservationDetail reservationDetail = reservationDetailRepository.findById(cancelDto.getReservationDetailId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        reservationDetail.setStatus("취소");
        reservationDetail.setCancelReason(cancelDto.getCancelReason());
        ReservationDetail savedReservationDetail = reservationDetailRepository.save(reservationDetail);

        // 연관된 모든 리플레이 삭제
        List<Replay> replays = replayRepository.findByReservationDetail_ReservationDetailId(reservationDetail.getReservationDetailId());
        for (Replay replay : replays) {
            replayService.deleteReplay(replay.getReplayId());
        }

        return convertToDto(savedReservationDetail);
    }

    /**
     * 예약 ID로 예약 취소 처리 (상태만 '취소'로 변경)
     * @deprecated 취소 사유를 받는 메소드로 대체됨 {@link #cancelReservation(ReservationCancelDto)}
     */
    @Transactional
    @Deprecated
    public ReservationResponseDto cancelReservation(Long reservationId) {
        ReservationDetail reservationDetail = reservationDetailRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        reservationDetail.setStatus("취소");
        ReservationDetail savedReservationDetail = reservationDetailRepository.save(reservationDetail);

        return convertToDto(savedReservationDetail);
    }

    /**
     * Reservation 엔티티를 ReservationResponseDto로 변환
     */
    private ReservationResponseDto convertToDto(ReservationDetail reservationDetail) {
        // 새 DTO 객체 생성
        ReservationResponseDto dto = new ReservationResponseDto();

        // 기본 필드 설정
        dto.setReservationDetailId(reservationDetail.getReservationDetailId());
        dto.setMemberId(reservationDetail.getMember().getMemberId());
        dto.setMemberName(reservationDetail.getMember().getName());
        dto.setInstructorId(reservationDetail.getInstructor().getInstructorId());
        dto.setInstructorName(reservationDetail.getInstructor().getMember().getName());
        dto.setLectureId(reservationDetail.getLecture().getId());
        dto.setDate(reservationDetail.getDate());
        dto.setStatus(reservationDetail.getStatus());
        dto.setRequestDetail(reservationDetail.getRequestDetail());
        dto.setScheduleDate(reservationDetail.getScheduleDate());
        dto.setCancelReason(reservationDetail.getCancelReason());

        // null 체크 추가, 유미
        if (reservationDetail.getLecture() != null &&
                reservationDetail.getLecture().getInfo() != null) {
            dto.setLectureTitle(reservationDetail.getLecture().getInfo().getTitle());
        } else {
            dto.setLectureTitle(""); // 정보가 없을 경우 빈 문자열로 설정
        }

        // 리플레이 정보 추가 (엔티티 관계를 통해 직접 조회)
        if (reservationDetail.getReplay() != null) {
            dto.setReplayId(reservationDetail.getReplay().getReplayId());
            dto.setReplayUrl(reservationDetail.getReplay().getFileUrl());
        }

        return dto;
    }
}