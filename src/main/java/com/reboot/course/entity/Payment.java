package com.reboot.course.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private Integer price;
    private LocalDateTime date;
    private String method;
    private String status;
}
