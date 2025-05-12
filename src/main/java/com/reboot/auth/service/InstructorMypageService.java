package com.reboot.auth.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class InstructorMypageService {

    private final InstructorRepository instructorRepository;
    private final MemberRepository memberRepository;

    public InstructorMypageService(InstructorRepository instructorRepository, MemberRepository memberRepository) {
        this.instructorRepository = instructorRepository;
        this.memberRepository = memberRepository;
    }

    public boolean isInstructor(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return instructorRepository.existsByMember(member);
    }

    public Instructor getInstructor(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return instructorRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("강사 정보를 찾을 수 없습니다."));
    }
}