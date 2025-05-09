package com.reboot.auth.service;

import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    // 기본 프로필 이미지 파일명 설정
    @Value("${default.profile.image:default-profile.png}")
    private String defaultProfileImage;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Member register(/* 회원 가입에 필요한 파라미터들 */) {
        // 기존 회원 가입 로직...

        Member member = new Member();
        // 사용자 정보 설정
        // member.setUsername(...);
        // member.setPassword(passwordEncoder.encode(...));
        // 기타 필드 설정...

        // 기본 프로필 이미지 설정
        String defaultImageUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/profiles/" + defaultProfileImage;
        member.setProfileImage(defaultImageUrl);

        return memberRepository.save(member);
    }
}