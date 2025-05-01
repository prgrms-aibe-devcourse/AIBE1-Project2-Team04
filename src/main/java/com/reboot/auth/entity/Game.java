package com.reboot.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long gameId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "game_type")
    private String gameType;

    @Column(name = "game_tier")
    private String gameTier;

    @Column(name = "game_position")
    private String gamePosition;
}