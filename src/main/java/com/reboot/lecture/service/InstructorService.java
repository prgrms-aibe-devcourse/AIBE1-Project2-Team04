package com.reboot.lecture.service;

import com.reboot.lecture.dto.InstructorResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InstructorService {

    // 모든 강사 목록 조회 (정렬 옵션 적용)
    Page<InstructorResponseDto> getAllInstructors(String sortBy, Pageable pageable);

    // 특정 게임 타입에 전문성을 가진 강사 목록 조회
    List<InstructorResponseDto> getInstructorsByGameType(String gameType);

    // 특정 포지션에 전문성을 가진 강사 목록 조회
    List<InstructorResponseDto> getInstructorsByPosition(String position);

    // 특정 티어에 해당하는 강사 목록 조회 (Game 테이블 사용)
    List<InstructorResponseDto> getInstructorsByGameTier(String gameTier);

    // 강사 검색 (닉네임 또는 소개글 기준)
    List<InstructorResponseDto> searchInstructors(String keyword);

    // 페이징 지원 강사 검색 메서드
    Page<InstructorResponseDto> searchInstructorsWithPaging(String keyword, Pageable pageable);

    // 페이징 지원 티어별 강사 목록 조회 (Game 테이블 사용)
    Page<InstructorResponseDto> getInstructorsByGameTierWithPaging(String gameTier, Pageable pageable);

    // 강사 상세 정보 조회
    InstructorResponseDto getInstructorById(Long instructorId);
}