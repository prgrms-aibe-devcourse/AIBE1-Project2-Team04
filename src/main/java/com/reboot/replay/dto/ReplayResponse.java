package com.reboot.replay.dto;

import lombok.*;

import java.time.LocalDateTime;

// 응답 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplayResponse {
    private Long replayId;
    private Long reservationDetailId;
    private String fileUrl;
    private LocalDateTime date;

    // YouTube 영상 ID를 추출하는 메소드
    public String getYoutubeVideoId() {
        if (fileUrl != null && fileUrl.contains("youtube.com/watch?v=")) {
            String videoId = fileUrl.split("v=")[1];
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return videoId;
        } else if (fileUrl != null && fileUrl.contains("youtu.be/")) {
            String videoId = fileUrl.split("youtu.be/")[1];
            if (videoId.contains("?")) {
                videoId = videoId.substring(0, videoId.indexOf("?"));
            }
            return videoId;
        }
        return null;
    }

    // YouTube 임베드 URL 생성 메소드
    public String getYoutubeEmbedUrl() {
        String videoId = getYoutubeVideoId();
        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId;
        }
        return null;
    }
}