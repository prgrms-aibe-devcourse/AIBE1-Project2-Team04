package com.reboot.instructor.entity;

import com.reboot.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instructor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instructor_id")
    private Long instructorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "career")
    private String career;

    @Column(name = "description")
    private String description;

    @Column(name = "review_num")
    private Integer reviewNum;

    @Column(name = "rating")
    private Double rating;
}