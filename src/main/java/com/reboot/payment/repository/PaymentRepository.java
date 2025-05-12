package com.reboot.payment.repository;

import com.reboot.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findById(Long paymentId); // 단일 객체 반환

    // Mypage용 조회쿼리 (결제완료 목록)
    List<Payment> findCompletedPaymentsByMember(@Param("memberId") Long memberId);

}