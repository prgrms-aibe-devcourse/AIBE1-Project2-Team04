package com.reboot.lecture.repository;

import com.reboot.lecture.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByLectureLectureId(Long lectureId);

    List<Reservation> findByMemberMemberId(@Param("memberId") Long memberId);

    List<Reservation> findByInstructorInstructorId(@Param("instructorId") Long instructorId);
}