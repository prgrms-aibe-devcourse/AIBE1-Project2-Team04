package com.reboot.payment.dto;

public record TossPaymentRequestDto(
        String orderNo,
        Integer amount,
        Integer amountTaxFree,
        String productDesc,
        String apiKey,
        Boolean autoExecute,
        String resultCallback,
        String retUrl,
        String retCancelUrl
) {}