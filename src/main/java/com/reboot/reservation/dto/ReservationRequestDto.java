package com.reboot.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDto {
    private Long memberId;
    private Long instructorId;
    private String lectureId;
    private String requestDetail; // 요청사항
    private String scheduleDate;  // 강의 일정 (yyyy-MM-dd)
}