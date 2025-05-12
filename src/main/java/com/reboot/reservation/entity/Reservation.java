package com.reboot.reservation.entity;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.lecture.entity.Lecture;
import com.reboot.payment.entity.Payment;
import com.reboot.replay.entity.Replay;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @ManyToOne
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    private String requestDetail;

    private String scheduleDate;  // yyyy-MM-dd 형태 권장

    private LocalDateTime date;

    private String status;

    private String cancelReason;  // 예약 취소 사유

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    @Builder.Default  // 이 어노테이션 추가
    private List<Replay> replays = new ArrayList<>();

    public Replay getReplay() {
        return replays != null && !replays.isEmpty() ? replays.get(0) : null;
    }
}