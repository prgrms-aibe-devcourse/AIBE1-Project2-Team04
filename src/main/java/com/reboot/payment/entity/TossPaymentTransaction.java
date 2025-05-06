package com.reboot.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tossPaymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String orderNo;
    private Integer amount;
    private Integer amountTaxFree;
    private String productDesc;
    private String apiKey;
    private Boolean autoExecute;
    private String resultCallback;
    private String retUrl;
    private String retCancelUrl;
    private String payToken;
    private String checkoutPage;
    private String paymentKey;
    private String payMethod;
    private String cardCompany;
    private String bankCode;
    private String responseCode;
    private String responseMsg;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private String status;
}