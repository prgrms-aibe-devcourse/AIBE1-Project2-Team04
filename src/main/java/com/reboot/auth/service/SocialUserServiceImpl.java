package com.reboot.auth.service;

import com.reboot.auth.entity.SocialUser;
import com.reboot.auth.oauth2.OAuthAttributes;
import com.reboot.auth.repository.SocialUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SocialUserServiceImpl implements SocialUserService {

    private final SocialUserRepository socialUserRepository;

    public SocialUserServiceImpl(SocialUserRepository socialUserRepository) {
        this.socialUserRepository = socialUserRepository;
    }

    @Override
    public Optional<SocialUser> findByProviderAndProviderId(String provider, String providerId) {
        return socialUserRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public SocialUser registerNewSocialUser(OAuthAttributes attributes) {
        SocialUser socialUser = SocialUser
                .builder()
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .email(attributes.getEmail())
                .name(attributes.getName())
                .build();
        return socialUserRepository.save(socialUser);
    }

    @Override
    public SocialUser updateExistingUser(SocialUser existing, OAuthAttributes attributes) {
        existing.setName(attributes.getName());
        existing.setEmail(attributes.getEmail());
        return socialUserRepository.save(existing);
    }

    @Override
    public boolean existsByEmail(String email) {
        return socialUserRepository.existsByEmail(email);
    }
}
