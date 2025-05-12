package com.reboot.reservation.repository;

import com.reboot.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 회원의 예약 전체 조회
    List<Reservation> findByMember_MemberId(Long memberId);

    Optional<Reservation> findByReservationId(Long reservationId);

    List<Reservation> findByMember_MemberIdAndLectureId(Long memberId, Long lectureId);
    // 수정된 메소드 (Lecture 엔티티의 id 필드 사용)
    List<Reservation> findByLectureId(Long lectureId);
}