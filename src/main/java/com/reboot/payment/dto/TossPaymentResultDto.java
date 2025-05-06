package com.reboot.payment.dto;

public record TossPaymentResultDto(
        String status,         // PAY_COMPLETE, PAY_APPROVED 등
        String orderNo,
        String payMethod,
        String cardCompany,    // 카드 결제시
        String bankCode        // 토스머니 결제시
) {}