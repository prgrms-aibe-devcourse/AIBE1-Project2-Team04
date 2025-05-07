package com.reboot.reservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private String requestDetail;
    private String scheduleDate;  // 강의 일정 (yyyy-MM-dd 등)
    private LocalDateTime date;
    private String status;
    private String PaymentStatus;

    //추후 기능구현
//    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
//    private Payment payment;
}