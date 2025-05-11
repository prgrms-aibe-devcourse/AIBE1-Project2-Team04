package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImage;

    private String phone;

    private String role;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Game game;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Instructor instructor;
}