package com.reboot.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefundRequestDto {
    private String apikey;
    private String payToken;

}