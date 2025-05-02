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
    private Long memberId;
    private String memberName;
    private Long instructorId;
    private String instructorName;
    private Long lectureId;
    private String lectureTitle;
    private LocalDateTime date;
    private String status;

}