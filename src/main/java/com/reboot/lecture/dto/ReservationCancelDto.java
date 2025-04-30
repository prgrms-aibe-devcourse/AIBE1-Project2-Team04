package com.reboot.lecture.dto;

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