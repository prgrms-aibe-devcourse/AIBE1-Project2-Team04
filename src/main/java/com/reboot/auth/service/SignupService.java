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

    public void signupProcess(SignupDTO signupDTO) {
        String username = signupDTO.username();
        String password = signupDTO.password();

        boolean isExist = memberRepository.existsByUsername(username);

        if (!isExist) {
            System.out.println("이미 등록된 사용자 : %s".formatted(username));
            return;
        }

        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(signupDTO.name());
        member.setEmail(signupDTO.email());
        member.setNickname(signupDTO.nickname());
        member.setRole("ADMIN");
        memberRepository.save(member);
    }
}
