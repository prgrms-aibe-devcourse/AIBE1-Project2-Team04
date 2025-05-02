package com.reboot.course.dto;

import lombok.*;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    private Long reservationId;
    private String method;
}