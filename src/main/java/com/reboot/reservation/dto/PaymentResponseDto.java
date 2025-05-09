package com.reboot.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private Long paymentId;
    private Long reservationDetailId;
    private Integer price;
    private LocalDateTime date;
    private String method;
    private String status;
}