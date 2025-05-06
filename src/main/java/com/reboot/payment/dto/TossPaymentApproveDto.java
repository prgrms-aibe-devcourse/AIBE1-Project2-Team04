package com.reboot.payment.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentApproveDto {
    private String paymentKey;
    private String orderNo;
    private Integer amount;
}