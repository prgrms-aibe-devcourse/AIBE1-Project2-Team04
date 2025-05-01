package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Instructor {

    @Id
    @Column(name = "instructor_id")
    private Long instructorId;

    @OneToOne
    @JoinColumn(name = "instructor_id")
    private Member member;

    @Column(name = "member_id", insertable = false, updatable = false)
    private Long memberId;

    // 강사정보
    @Column(length = 1000)
    private String career;

    @Column(length = 2000)
    private String description;

    @Column(name = "review_num")
    private int reviewNum = 0;

    @Column(name = "rating")
    private double rating = 0.0;
}