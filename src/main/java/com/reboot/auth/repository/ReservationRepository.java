package com.reboot.auth.repository;

import com.reboot.auth.entity.ReservationMy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationMy, Long> {
    List<ReservationMy> findByMemberId(Long memberId);
    List<ReservationMy> findByInstructorId(Long instructorId);
    List<ReservationMy> findByLectureId(Long lectureId);
    List<ReservationMy> findByStatus(String status);
}