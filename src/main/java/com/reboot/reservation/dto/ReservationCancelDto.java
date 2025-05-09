package com.reboot.reservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCancelDto {
    private Long reservationDetailId;
    private String cancelReason;
}