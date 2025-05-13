package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider; // 예: "google", "naver", "kakao"

    @Column(nullable = false)
    private String providerId; // 각 프로바이더 내에서의 사용자 고유 ID

    private String email;

    private String name;
}
