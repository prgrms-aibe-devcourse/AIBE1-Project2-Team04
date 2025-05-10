package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationMy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "instructor_id")
    private Long instructorId;

    @Column(name = "lecture_id")
    private Long lectureId;

    private LocalDateTime date;

    private String status;
}