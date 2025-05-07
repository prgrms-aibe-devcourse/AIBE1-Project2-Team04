package com.reboot.payment.dto;

public record TossApproveDto(
        String paymentKey,
        String orderNo,
        Integer amount
) {}