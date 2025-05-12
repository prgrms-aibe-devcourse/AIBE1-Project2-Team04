package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String expiration;
}