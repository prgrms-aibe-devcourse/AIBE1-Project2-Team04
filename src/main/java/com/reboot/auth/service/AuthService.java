package com.reboot.auth.service;

import com.reboot.auth.entity.Instructor;
import com.reboot.auth.entity.Member;
import com.reboot.auth.exception.InstructorNotFoundException;
import com.reboot.auth.repository.InstructorRepository;
import com.reboot.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;


// 인증 관련 서비스
// 현재 로그인한 사용자의 정보를 제공하고 처리하는 서비스
@Service
@RequiredArgsConstructor
public class AuthService {

    private final InstructorRepository instructorRepository;
    private final MemberRepository memberRepository;


    // 현재 로그인한 사용자의 ID(username)를 반환
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return authentication.getName(); // Spring Security에서는 username이 사용자 ID로 사용됨
    }


    // 현재 로그인한 사용자의 Member 정보를 조회
    @Transactional(readOnly = true)
    public Member getCurrentMember() {
        String userName = getCurrentUserId();
        return memberRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다: " + userName));
    }


    // 현재 로그인한 강사 정보를 조회
    // 현재 로그인한 사용자 ID를 기반으로 강사 정보를 데이터베이스에서 조회
    @Transactional(readOnly = true)
    public Instructor getCurrentInstructor() {
        Member member = getCurrentMember();
        return instructorRepository.findById(member.getMemberId())
                .orElseThrow(() -> new InstructorNotFoundException("강사 정보를 찾을 수 없습니다: " + member.getMemberId()));
    }


    // 강사 ID로 강사 정보 조회
    @Transactional(readOnly = true)
    public Instructor getInstructorById(Long instructorId) {
        return instructorRepository.findById(instructorId)
                .orElseThrow(() -> new InstructorNotFoundException("강사 정보를 찾을 수 없습니다: " + instructorId));
    }


    // 현재 사용자가 특정 강사인지 확인
    public boolean isCurrentInstructor(Long instructorId) {
        try {
            Member member = getCurrentMember();
            return member.getMemberId().equals(instructorId);
        } catch (Exception e) {
            return false;
        }
    }
}