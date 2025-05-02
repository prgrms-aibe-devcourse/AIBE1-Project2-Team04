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
    private Long lectureId;
    private String requestDetail; // 요청사항
    private String scheduleDate;  // 강의 일정 (yyyy-MM-dd)

    public ReservationRequestDto(Long memberId, Long instructorId, Long lectureId) {
        this.memberId = memberId;
        this.instructorId = instructorId;
        this.lectureId = lectureId;
    }
}