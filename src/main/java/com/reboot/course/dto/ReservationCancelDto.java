package com.reboot.course.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCancelDto {
    private Long reservationId;
    private String cancelReason;
}
