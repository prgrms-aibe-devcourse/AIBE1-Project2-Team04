package com.reboot.payment.repository;

import com.reboot.payment.entity.Payment;
import com.reboot.payment.entity.RefundHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<RefundHistory, Long> {
    List<RefundHistory> findByPayment(Payment payment);
    Optional<RefundHistory> findByRefundNo(String refundNo);
}