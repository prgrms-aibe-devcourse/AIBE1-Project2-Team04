package com.reboot.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ReservationRequestDto {
    private Long memberId;
    private Long instructorId;
    private Long lectureId;

    public ReservationRequestDto(Long memberId, Long instructorId, Long lectureId) {
        this.memberId = memberId;
        this.instructorId = instructorId;
        this.lectureId = lectureId;
    }
}