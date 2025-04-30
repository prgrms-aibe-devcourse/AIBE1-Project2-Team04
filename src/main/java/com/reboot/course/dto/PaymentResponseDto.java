package com.reboot.course.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private Long paymentId;
    private Long reservationId;
    private Integer price;
    private LocalDateTime date;
    private String method;
    private String status;
}
