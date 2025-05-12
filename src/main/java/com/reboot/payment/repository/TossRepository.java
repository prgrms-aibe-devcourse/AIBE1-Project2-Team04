package com.reboot.payment.repository;

import com.reboot.payment.entity.Payment;
import com.reboot.payment.entity.TossTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossRepository extends JpaRepository<TossTransaction, Long> {
    Optional<TossTransaction> findByPayment(Payment payment);
    Optional<TossTransaction> findByOrderNo(String orderNo);
}