/*
package com.reboot.auth.service;

import com.reboot.auth.dto.SignupDTO;
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

    public boolean signupProcess(SignupDTO signupDTO) {
        if (isUsernameExist(signupDTO.username())) {
            System.out.printf("이미 등록된 사용자 : %s", signupDTO.username());
            return false;
        }

        Member member = createMember(signupDTO);
        memberRepository.save(member);
        return true;
    }

    private boolean isUsernameExist(String username) {
        return memberRepository.existsByUsername(username);
    }

    private Member createMember(SignupDTO dto) {
        Member member = new Member();
        member.setUsername(dto.username());
        member.setPassword(passwordEncoder.encode(dto.password()));
        member.setName(dto.name());
        member.setEmail(dto.email());
        member.setNickname(dto.nickname());
        member.setRole("USER");
        return member;
    }
}*/
