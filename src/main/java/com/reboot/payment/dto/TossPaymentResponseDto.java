package com.reboot.payment.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentResponseDto {
    private int code;              // 0: 성공, 그 외: 실패
    private String checkoutPage;   // 결제창 URL
    private String payToken;       // 거래 구분용 토스 고유값
    private String message;        // 실패시 메시지(선택)
}