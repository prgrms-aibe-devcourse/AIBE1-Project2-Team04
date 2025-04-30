package com.reboot.lecture.dto;

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
    private String studentName;
    private Long instructorId;
    private String instructorName;
    private Long lectureId;
    private String lectureTitle;
    private LocalDateTime date;
    private String status;
}