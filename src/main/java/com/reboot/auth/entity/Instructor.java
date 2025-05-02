package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "instructor")
@Data
public class Instructor {

    @Id
    @Column(name = "instructor_id")
    private Long instructorId;

    @Column(unique = true, nullable = false)
    private String nickname; // 강사 닉네임 추가 (강의 목록 표시에 필요)

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