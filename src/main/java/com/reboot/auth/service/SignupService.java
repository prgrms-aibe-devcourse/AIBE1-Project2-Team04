package com.reboot.auth.service;

import com.reboot.auth.dto.SignupDTO;
import com.reboot.auth.dto.SignupResponse;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void signupProcess(SignupDTO signupDTO) {
        Member member = createMember(signupDTO);
        memberRepository.save(member);
    }

    public SignupResponse validate(SignupDTO dto) {
        if (memberRepository.existsByUsername(dto.username())) {
            return new SignupResponse(false, "username", "이미 사용 중인 아이디입니다.");
        }

        if (memberRepository.existsByEmail(dto.email())) {
            return new SignupResponse(false, "email", "이미 사용 중인 이메일입니다.");
        }

        if (memberRepository.existsByNickname(dto.nickname())) {
            return new SignupResponse(false, "nickname", "이미 사용 중인 닉네임입니다.");
        }

        return new SignupResponse(true, "", "");
    }

    private Member createMember(SignupDTO dto) {
        Member member = new Member();
        member.setUsername(dto.username());
        member.setPassword(passwordEncoder.encode(dto.password()));
        member.setName(dto.name());
        member.setEmail(dto.email());
        member.setNickname(dto.nickname());
        member.setProfileImage(dto.profile_image());
        member.setPhone(dto.phone());
        member.setRole(dto.role());
        return member;
    }
}
