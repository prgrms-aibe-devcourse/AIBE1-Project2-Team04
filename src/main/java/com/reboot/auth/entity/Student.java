package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "student")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    //인증관련
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    //기본정보
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    private String phone;

}
