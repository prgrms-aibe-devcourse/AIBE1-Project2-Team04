package com.reboot.course.dto;

import lombok.*;

@Getter
@NoArgsConstructor
public class RefundRequestDto {
    private Long paymentId;
    private String refundReason;
}
