package com.reboot.replay.entity;

import com.reboot.reservation.entity.ReservationDetail;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Replay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "replay_id")
    private Long replayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private ReservationDetail reservationDetail;

    @Column(name = "file_url")
    private String fileUrl;  // YouTube URL을 저장할 필드

    @Column(name = "date")
    private LocalDateTime date;
}