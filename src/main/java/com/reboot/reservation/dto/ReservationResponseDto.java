package com.reboot.reservation.dto;

import com.reboot.replay.dto.ReplayResponse;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReservationResponseDto {
    private Long reservationId;
    private Long memberId;
    private String memberName;
    private Long instructorId;
    private String instructorName;
    private Long lectureId;
    private String lectureTitle;
    private LocalDateTime date;
    private String status;
    private String requestDetail;
    private String scheduleDate;
    private String cancelReason;

    // 리플레이 정보 추가
    private Long replayId;
    private String replayUrl;

    // 리플레이 목록 추가 (필드 위치 이동)
    @Builder.Default
    private List<ReplayResponse> replays = new ArrayList<>();

    // replayId가 존재하는지 확인하는 헬퍼 메소드
    public boolean hasReplay() {
        return replayId != null;
    }

    // YouTube 임베드 URL로 변환하는 메소드
    public String getYoutubeEmbedUrl() {
        if (replayUrl == null) return null;

        String videoId = null;

        if (replayUrl.contains("youtube.com/watch?v=")) {
            videoId = replayUrl.split("v=")[1];
            int ampersandPos = videoId.indexOf('&');
            if (ampersandPos != -1) {
                videoId = videoId.substring(0, ampersandPos);
            }
        } else if (replayUrl.contains("youtu.be/")) {
            videoId = replayUrl.split("youtu.be/")[1];
            int questionMarkPos = videoId.indexOf('?');
            if (questionMarkPos != -1) {
                videoId = videoId.substring(0, questionMarkPos);
            }
        }

        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId;
        }

        return null;
    }
}