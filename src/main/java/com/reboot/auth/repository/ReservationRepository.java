package com.reboot.auth.repository;

import com.reboot.auth.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMemberId(Long memberId);
    List<Reservation> findByInstructorId(Long instructorId);
    List<Reservation> findByLectureId(Long lectureId);
    List<Reservation> findByStatus(String status);
}
