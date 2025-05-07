package com.reboot.reservation.repository;

import com.reboot.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 회원의 예약 전체 조회
    List<Reservation> findByMemberMemberId(Long memberId);

    List<Reservation> findByMemberMemberIdAndLectureId(Long memberId, Long lectureId);
    // 수정된 메소드 (Lecture 엔티티의 id 필드 사용)
    List<Reservation> findByLectureId(Long lectureId);
}