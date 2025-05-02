package com.reboot.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ReservationCancelDto {
    private Long reservationId;
    private String cancelReason;
}