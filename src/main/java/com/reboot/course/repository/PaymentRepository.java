package com.reboot.course.repository;

import com.reboot.course.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 예약 ID로 결제 내역 조회
    Payment findByReservationReservationId(Long reservationId);
}