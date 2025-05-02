package com.reboot.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true)
    private String username;

    private String password;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private String phone;
    private String role;
    private String customService; // 맞춤 강사 서비스

    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations;
}