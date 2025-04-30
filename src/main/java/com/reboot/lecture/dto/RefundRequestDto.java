package com.reboot.lecture.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequestDto {
    private Long paymentId;
    private String refundReason;
}