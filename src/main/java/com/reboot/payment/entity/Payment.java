package com.reboot.payment.entity;

import com.reboot.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private Integer price;

    private LocalDateTime date;

    private String method; // 예: CARD, TOSS_MONEY 등

    private String status; // 예: READY, COMPLETE, FAIL 등
}