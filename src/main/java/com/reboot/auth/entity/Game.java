package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String gameType;

    private String gameTier;

    private String gamePosition;
}