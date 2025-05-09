package com.reboot.auth.service;

import com.reboot.auth.dto.InstructorResponseDto;
import com.reboot.auth.entity.Instructor;
import com.reboot.auth.exception.InstructorNotFoundException;
import com.reboot.auth.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorServiceImpl implements com.reboot.auth.service.InstructorService {

    private final InstructorRepository instructorRepository;

    // 모든 강사 목록 조회 (정렬 옵션 적용)
    @Override
    public Page<InstructorResponseDto> getAllInstructors(String sortBy, Pageable pageable) {
        Page<Instructor> instructors;

        // 정렬 기준에 따라 적절한 리포지토리 메서드 호출
        switch (sortBy) {
            case "newest":
                // 최신 등록순
                instructors = instructorRepository.findByOrderByCreatedAtDesc(pageable);
                break;
            case "reviews":
                // 리뷰 많은순
                instructors = instructorRepository.findByOrderByReviewNumDesc(pageable);
                break;
            case "students":
                // 수강생 많은순
                instructors = instructorRepository.findByOrderByTotalMembersDesc(pageable);
                break;
            case "popularity":
            default:
                // 인기순 (기본)
                instructors = instructorRepository.findByTrueOrderByPopularityDesc(pageable);
                break;
        }

        return instructors.map(this::convertToDto);
    }

    // 특정 게임 타입에 전문성을 가진 강사 목록 조회
    @Override
    public List<InstructorResponseDto> getInstructorsByGameType(String gameType) {
        List<Instructor> instructors = instructorRepository.findByExpertiseGameType(gameType);
        return instructors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 포지션에 전문성을 가진 강사 목록 조회
    @Override
    public List<InstructorResponseDto> getInstructorsByPosition(String position) {
        List<Instructor> instructors = instructorRepository.findByExpertisePosition(position);
        return instructors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 티어에 해당하는 강사 목록 조회 (Game 테이블 사용)
    @Override
    public List<InstructorResponseDto> getInstructorsByGameTier(String gameTier) {
        List<Instructor> instructors = instructorRepository.findByGameTier(gameTier);
        return instructors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 강사 검색 (닉네임 또는 소개글 기준)
    @Override
    public List<InstructorResponseDto> searchInstructors(String keyword) {
        List<Instructor> instructors = instructorRepository.searchInstructors(keyword);
        return instructors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 페이징 지원 강사 검색 메서드
    @Override
    public Page<InstructorResponseDto> searchInstructorsWithPaging(String keyword, Pageable pageable) {
        Page<Instructor> instructors = instructorRepository.searchInstructors(keyword, pageable);
        return instructors.map(this::convertToDto);
    }

    // 페이징 지원 티어별 강사 목록 조회 (Game 테이블 사용)
    @Override
    public Page<InstructorResponseDto> getInstructorsByGameTierWithPaging(String gameTier, Pageable pageable) {
        Page<Instructor> instructors = instructorRepository.findByGameTier(gameTier, pageable);
        return instructors.map(this::convertToDto);
    }

    // 강사 상세 정보 조회
    @Override
    public InstructorResponseDto getInstructorById(Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new InstructorNotFoundException("강사를 찾을 수 없습니다: " + instructorId));
        return convertToDto(instructor);
    }

    // Instructor 엔티티를 DTO로 변환
    private InstructorResponseDto convertToDto(Instructor instructor) {
        return InstructorResponseDto.builder()
                .instructorId(instructor.getInstructorId())
                .nickname(instructor.getNickname())
                .career(instructor.getCareer())
                .description(instructor.getDescription())
                .reviewNum(instructor.getReviewNum())
                .averageRating(instructor.getAverageRating())
                .totalMembers(instructor.getTotalMembers())
                .createdAt(instructor.getCreatedAt())
                .gameType(instructor.getGameType()) // Game 정보 추가
                .gamePosition(instructor.getGamePosition()) // Game 정보 추가
                .gameTier(instructor.getGameTier()) // Game 정보 추가
                .build();
    }
}