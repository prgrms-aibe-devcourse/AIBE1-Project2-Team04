package com.reboot.auth.repository;

import com.reboot.auth.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {
    Optional<SocialUser> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByEmail(String email);
}
