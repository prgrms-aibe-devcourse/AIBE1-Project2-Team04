package com.reboot.auth.repository;

import com.reboot.auth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    boolean existsByToken(String token);

    @Transactional
    void deleteByToken(String token);

}
