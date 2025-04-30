package com.reboot.course.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDto {
    private Long reservationId;
    private Long studentId;
    private Long instructorId;
    private Long lectureId;
    private LocalDateTime date;
    private String status;
}
