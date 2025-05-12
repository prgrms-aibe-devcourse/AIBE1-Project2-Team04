package com.reboot.payment.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossRequestDto {
    private String orderNo;
    private Integer amount;
    private Integer amountTaxFree;
    private String productDesc;
    private String apiKey;
    private Boolean autoExecute;
    private String resultCallback;
    private String retUrl;
    private String retCancelUrl;
}