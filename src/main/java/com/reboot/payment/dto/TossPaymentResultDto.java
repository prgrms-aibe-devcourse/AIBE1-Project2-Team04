package com.reboot.payment.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentResultDto {
    private String status;         // PAY_COMPLETE, PAY_APPROVED 등
    private String orderNo;
    private String payMethod;
    private String cardCompany;    // 카드 결제시
    private String bankCode;       // 토스머니 결제시
}