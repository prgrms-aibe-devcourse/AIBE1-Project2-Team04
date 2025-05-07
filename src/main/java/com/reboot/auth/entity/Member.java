package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "member")
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    //인증관련
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    //기본정보
    @Column(nullable = false)
    private String name;

    @Column(name ="e_mail",nullable = false, unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    private String phone;

    @Column(name = "role")
    private String role;

}
