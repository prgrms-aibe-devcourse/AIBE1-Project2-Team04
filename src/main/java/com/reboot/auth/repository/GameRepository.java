package com.reboot.auth.repository;


import com.reboot.auth.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByMemberId(Long memberId);
    Optional<Game> findByGameIdAndMemberId(Long gameId, Long memberId);
    List<Game> findByGameType(String gameType);
    List<Game> findByMemberIdAndGameType(Long memberId, String gameType);
    List<Game> findByGameTier(String gameTier);
    List<Game> findByGamePosition(String gamePosition);
    boolean existsByMemberId(Long memberId);
    boolean existsByMemberIdAndGameType(Long memberId, String gameType);
    void deleteByMemberId(Long memberId);
}
