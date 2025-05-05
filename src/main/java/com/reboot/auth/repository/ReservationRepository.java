package com.reboot.auth.repository;

import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepository {

    List<Reservation> findByMemberId(Long memberId);
    List<Reservation> findByInstructorId(Long instructorId);
    List<Reservation> findByLectureId(Long lectureId);
    List<Reservation> findByStatus(String status);
}
