package com.reboot.lecture.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private Long reservationId;
    private Integer price;
    private String method;
}