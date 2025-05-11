package com.reboot.auth.repository;

import com.reboot.auth.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {


    // 멤버 ID로 게임 정보를 조회합니다.
    List<Game> findByMember_MemberId(Long memberId);


    // 게임 타입으로 게임 목록을 조회합니다.
    List<Game> findByGameType(String gameType);


    // 게임 티어로 게임 목록을 조회합니다.
    List<Game> findByGameTier(String gameTier);


    // 게임 포지션으로 게임 목록을 조회합니다.
    List<Game> findByGamePosition(String gamePosition);


    // 게임 타입과 티어로 게임 목록을 조회합니다.
    List<Game> findByGameTypeAndGameTier(String gameType, String gameTier);


    // 게임 타입과 포지션으로 게임 목록을 조회합니다.
    List<Game> findByGameTypeAndGamePosition(String gameType, String gamePosition);


    // 멤버 ID로 게임 존재 여부를 확인합니다.
    boolean existsByMemberMemberId(Long memberId);
}