package com.reboot.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lectureId;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    private String title;
    private String description;
    private String gameType;
    private Integer price;
}