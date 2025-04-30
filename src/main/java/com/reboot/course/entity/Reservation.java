package com.reboot.course.entity;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Student;
import com.reboot.replay.entity.Replay;
import com.reboot.review.entity.Review;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "member_id")
    private Member memberId;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructorId;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lectureId;

    private LocalDateTime date;
    private String status;

    // 일단 아래는 필요하신 분이 활성화하면 될 것 같아요.
//    @OneToOne(mappedBy = "reservation")
//    private Payment payment;
//
//    @OneToOne(mappedBy = "reservation")
//    private Review review;
//
//    @OneToOne(mappedBy = "reservation")
//    private Replay replay;
}
