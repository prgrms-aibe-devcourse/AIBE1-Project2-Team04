package com.reboot.auth.service;

import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public String getUsernameByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(Member::getUsername)
                .orElseThrow(() -> new RuntimeException("해당 이메일의 사용자가 없습니다: " + email));
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
}