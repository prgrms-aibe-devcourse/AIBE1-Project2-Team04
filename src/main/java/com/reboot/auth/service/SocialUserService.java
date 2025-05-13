package com.reboot.auth.service;

import com.reboot.auth.entity.SocialUser;
import com.reboot.auth.oauth2.OAuthAttributes;

import java.util.Optional;

public interface SocialUserService {
    Optional<SocialUser> findByProviderAndProviderId(String provider, String providerId);
    SocialUser registerNewSocialUser(OAuthAttributes attributes);
    SocialUser updateExistingUser(SocialUser existing, OAuthAttributes attributes);
    boolean existsByEmail(String email);
}
