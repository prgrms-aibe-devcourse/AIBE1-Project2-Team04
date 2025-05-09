package com.reboot.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refund_history")
public class RefundHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String refundNo;
    private Integer refundAmount;
    private String refundReason;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String status;
    // 토스 API 응답 관련 필드
    private String responseCode;
    private String responseMessage;
}