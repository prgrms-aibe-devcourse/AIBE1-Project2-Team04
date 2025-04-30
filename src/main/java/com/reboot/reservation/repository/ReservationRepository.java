package com.reboot.reservation.repository;

import com.reboot.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMemberMemberId(Long memberId);
    List<Reservation> findByInstructorInstructorId(Long instructorId);
    List<Reservation> findByLectureLectureId(Long lectureId);
}