package com.reboot.reservation.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDto {
    private Long memberId;
    private Long instructorId;
    private Long lectureId;
    private String requestDetail;
    private String scheduleDate;

    // 리플레이 정보 추가
    private String youtubeUrl;
}