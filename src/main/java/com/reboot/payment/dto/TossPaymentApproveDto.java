package com.reboot.payment.dto;

public record TossPaymentApproveDto(
        String paymentKey,
        String orderNo,
        Integer amount
) {}