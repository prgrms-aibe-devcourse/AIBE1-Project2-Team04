package com.reboot.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @Column(unique = true)
    private String refundNo;

    @Column
    private String reason;


}