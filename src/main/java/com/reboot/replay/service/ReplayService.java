package com.reboot.replay.service;

import com.reboot.replay.dto.ReplayRequest;
import com.reboot.replay.dto.ReplayResponse;
import com.reboot.replay.entity.Replay;
import com.reboot.replay.repository.ReplayRepository;
import com.reboot.reservation.entity.Reservation;
import com.reboot.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplayService {

    private final ReplayRepository replayRepository;
    private final ReservationRepository reservationRepository;

    // 리플레이 저장
    public ReplayResponse saveReplay(ReplayRequest request) {
        // 유튜브 URL 검증
        validateYoutubeUrl(request.getFileUrl());

        // 예약 존재 여부 확인
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("해당 예약이 존재하지 않습니다: " + request.getReservationId()));

        Replay replay = Replay.builder()
                .reservation(reservation)
                .fileUrl(request.getFileUrl())
                .date(LocalDateTime.now())
                .build();

        Replay savedReplay = replayRepository.save(replay);

        return convertToResponseDto(savedReplay);
    }

    // 모든 리플레이 목록 조회
    public List<ReplayResponse> getAllReplays() {
        List<Replay> replays = replayRepository.findAll();

        return replays.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 리플레이 조회
    public ReplayResponse getReplay(Long replayId) {
        Replay replay = replayRepository.findById(replayId)
                .orElseThrow(() -> new EntityNotFoundException("해당 리플레이가 존재하지 않습니다: " + replayId));

        return convertToResponseDto(replay);
    }

    // 리플레이 수정
    public ReplayResponse updateReplay(Long replayId, ReplayRequest request) {
        // 유튜브 URL 검증
        validateYoutubeUrl(request.getFileUrl());

        // 해당 ID의 리플레이 존재 확인
        Replay replay = replayRepository.findById(replayId)
                .orElseThrow(() -> new EntityNotFoundException("해당 리플레이가 존재하지 않습니다: " + replayId));

        // 파일 URL 업데이트
        replay.setFileUrl(request.getFileUrl());

        // 수정 시간 업데이트
        replay.setDate(LocalDateTime.now());

        Replay updatedReplay = replayRepository.save(replay);

        return convertToResponseDto(updatedReplay);
    }

    public void deleteReplay(Long replayId) {
        // 리플레이 존재 여부 확인
        if (!replayRepository.existsById(replayId)) {
            throw new EntityNotFoundException("해당 리플레이가 존재하지 않습니다: " + replayId);
        }

        // 리플레이 삭제
        replayRepository.deleteById(replayId);
    }

    // 예약 ID로 리플레이 목록 조회
    public List<ReplayResponse> getReplaysByReservationId(Long reservationId) {
        List<Replay> replays = replayRepository.findByReservationReservationId(reservationId);

        return replays.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 유튜브 URL 유효성 검사
    private void validateYoutubeUrl(String url) {
        if (url == null || (!url.contains("youtube.com/watch?v=") && !url.contains("youtu.be/"))) {
            throw new IllegalArgumentException("유효한 YouTube URL이 아닙니다.");
        }
    }

    // Entity를 DTO로 변환
    private ReplayResponse convertToResponseDto(Replay replay) {
        return ReplayResponse.builder()
                .replayId(replay.getReplayId())
                .reservationId(replay.getReservation().getReservationId())
                .fileUrl(replay.getFileUrl())
                .date(replay.getDate())
                .build();
    }
}