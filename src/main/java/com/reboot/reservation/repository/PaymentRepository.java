package com.reboot.reservation.repository;

import com.reboot.reservation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 예약 ID로 결제 내역 조회
    Payment findByReservationDetail_ReservationDetailId(Long reservationDetailId);
}