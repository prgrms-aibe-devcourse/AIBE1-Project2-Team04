package com.reboot.auth.oauth2;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String provider;
    private String providerId;

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return ofNaver("id", response);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle("sub", attributes);
    }

    private static OAuthAttributes ofGoogle(String key, Map<String, Object> attrs) {
        return OAuthAttributes.builder()
                .name((String) attrs.get("name"))
                .email((String) attrs.get("email"))
                .attributes(attrs)
                .nameAttributeKey(key)
                .providerId((String) attrs.get(key))
                .provider("google")
                .build();
    }

    private static OAuthAttributes ofNaver(String key, Map<String, Object> resp) {
        return OAuthAttributes.builder()
                .name((String) resp.get("name"))
                .email((String) resp.get("email"))
                .attributes(resp)
                .nameAttributeKey(key)
                .providerId((String) resp.get(key))
                .provider("naver")
                .build();
    }

    private static OAuthAttributes ofKakao(String key, Map<String,Object> attrs) {
        Map<String,Object> kakaoAcc = (Map<String,Object>) attrs.get("kakao_account");
        Map<String,Object> profile = (Map<String,Object>) kakaoAcc.get("profile");

        String email = (String) kakaoAcc.get("email");
        String name = (String) profile.get("nickname");

        Map<String, Object> flatAttributes = new HashMap<>(attrs);
        flatAttributes.put("email", email);
        flatAttributes.put("name", name);

        return OAuthAttributes.builder()
                .name(name)
                .email(email)
                .attributes(flatAttributes)
                .nameAttributeKey(key)
                .providerId(String.valueOf(attrs.get(key)))
                .provider("kakao")
                .build();
    }
}
