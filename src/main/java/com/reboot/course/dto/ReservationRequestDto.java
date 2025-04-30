package com.reboot.course.dto;

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
    private String status;
}
