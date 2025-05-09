package com.reboot.reservation.repository;

import com.reboot.reservation.entity.ReservationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    // 특정 회원의 예약 전체 조회
    List<ReservationDetail> findByMember_MemberId(Long memberId);

    Optional<ReservationDetail> findByReservationDetailId(Long reservationDetailId);

    List<ReservationDetail> findByMember_MemberIdAndLectureId(Long memberId, Long lectureId);
    // 수정된 메소드 (Lecture 엔티티의 id 필드 사용)
    List<ReservationDetail> findByLectureId(Long lectureId);
}