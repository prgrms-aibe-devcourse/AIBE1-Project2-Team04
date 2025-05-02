package com.reboot.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructorId;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String career;
    private String description;
    private Integer reviewNum;
    private Double rating;
}