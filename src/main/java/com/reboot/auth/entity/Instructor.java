package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "instructor")
@Data
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instructor_id")
    private Long id;

    //인증관련
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // 개인정보
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "profile_image")
    private String profileImage;

    // 강사정보
    private String career;
    private String description;

    @Column(name = "review_num")
    private Integer reviewNum = 0;
    private Double rating = 0.0;

}
