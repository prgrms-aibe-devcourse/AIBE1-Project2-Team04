package com.reboot.payment.dto;

public record TossPaymentResponseDto(
        int code,              // 0: 성공, 그 외: 실패
        String checkoutPage,   // 결제창 URL
        String payToken,       // 거래 구분용 토스 고유값
        String message         // 실패시 메시지(선택)
) {}