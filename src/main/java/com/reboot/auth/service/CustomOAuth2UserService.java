package com.reboot.auth.service;

import com.reboot.auth.entity.SocialUser;
import com.reboot.auth.oauth2.OAuthAttributes;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;

public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final SocialUserService socialUserService;

    public CustomOAuth2UserService(SocialUserService socialUserService) {
        this.socialUserService = socialUserService;
    }


    @SuppressWarnings("unchecked")
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = delegate.loadUser(request);
        String regId = request.getClientRegistration().getRegistrationId();
        OAuthAttributes attr = OAuthAttributes.of(regId, oAuth2User.getAttributes());

        SocialUser user = socialUserService.findByProviderAndProviderId(
                        attr.getProvider(), attr.getProviderId())
                .map(u -> socialUserService.updateExistingUser(u, attr))
                .orElseGet(() -> socialUserService.registerNewSocialUser(attr));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attr.getAttributes(),
                attr.getNameAttributeKey()
        );
    }
}
