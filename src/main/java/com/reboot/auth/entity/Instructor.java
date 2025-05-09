package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructorId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Column(length = 1000)
    private String career;

    @Column(length = 2000)
    private String description;

    private int reviewNum;

    private double averageRating;

    private LocalDateTime createdAt;

    private Integer totalMembers;
}
