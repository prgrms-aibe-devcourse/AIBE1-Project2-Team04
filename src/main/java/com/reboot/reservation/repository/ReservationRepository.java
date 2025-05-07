package com.reboot.reservation.repository;

import com.reboot.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 회원의 예약 전체 조회
    List<Reservation> findByMemberMemberId(Long memberId);

    // 특정 강사의 예약 전체 조회
    List<Reservation> findByInstructorInstructorId(Long instructorId);

    // 특정 강의의 예약 전체 조회
    List<Reservation> findByLectureLectureId(Long lectureId);

    List<Reservation> findByReservationId(Long reservationId);
}