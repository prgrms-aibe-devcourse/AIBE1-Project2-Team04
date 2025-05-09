package com.reboot.replay.repository;

import com.reboot.replay.entity.Replay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayRepository extends JpaRepository<Replay, Long> {
    List<Replay> findByReservationDetail_ReservationDetailId(Long reservationDetailId);
}