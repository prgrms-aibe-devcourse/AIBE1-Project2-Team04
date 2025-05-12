package com.reboot.lecture.controller;

import com.reboot.lecture.dto.InstructorResponseDto;
import com.reboot.lecture.service.InstructorService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@Tag(name = "강사 API", description = "강사 조회, 검색, 필터링 API")
@RestController
@RequestMapping("/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    // 모든 강사 목록 조회 (정렬 옵션 적용)
    @GetMapping
    @Operation(summary = "강사 목록 조회", description = "정렬 옵션이 적용된 강사 목록 조회")
    public ResponseEntity<Page<InstructorResponseDto>> getAllInstructors(
            @RequestParam(defaultValue = "popularity") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InstructorResponseDto> instructors = instructorService.getAllInstructors(sortBy, pageable);
        return ResponseEntity.ok(instructors);
    }

    // 특정 게임 타입에 전문성을 가진 강사 목록 조회
    @GetMapping("/game/{gameType}")
    @Operation(summary = "게임별 강사 목록 조회", description = "특정 게임 타입에 전문성을 가진 강사 목록 조회")
    public ResponseEntity<List<InstructorResponseDto>> getInstructorsByGameType(
            @PathVariable String gameType) {
        List<InstructorResponseDto> instructors = instructorService.getInstructorsByGameType(gameType);
        return ResponseEntity.ok(instructors);
    }

    // 특정 포지션에 전문성을 가진 강사 목록 조회
    @GetMapping("/position/{position}")
    @Operation(summary = "포지션별 강사 목록 조회", description = "특정 포지션에 전문성을 가진 강사 목록 조회")
    public ResponseEntity<List<InstructorResponseDto>> getInstructorsByPosition(
            @PathVariable String position) {
        List<InstructorResponseDto> instructors = instructorService.getInstructorsByPosition(position);
        return ResponseEntity.ok(instructors);
    }

    // 특정 티어에 해당하는 강사 목록 조회 (추가)
    @GetMapping("/tier/{gameTier}")
    @Operation(summary = "티어별 강사 목록 조회", description = "특정 티어에 해당하는 강사 목록 조회")
    public ResponseEntity<List<InstructorResponseDto>> getInstructorsByGameTier(
            @PathVariable String gameTier) {
        List<InstructorResponseDto> instructors = instructorService.getInstructorsByGameTier(gameTier);
        return ResponseEntity.ok(instructors);
    }

    // 강사 검색 (닉네임 또는 소개글 기준)
    @GetMapping("/search")
    @Operation(summary = "강사 검색", description = "닉네임 또는 소개글에 특정 키워드가 포함된 강사 검색")
    public ResponseEntity<List<InstructorResponseDto>> searchInstructors(
            @RequestParam String keyword) {
        List<InstructorResponseDto> instructors = instructorService.searchInstructors(keyword);
        return ResponseEntity.ok(instructors);
    }

    // 강사 상세 정보 조회
    @GetMapping("/{instructorId}")
    @Operation(summary = "강사 상세 정보 조회", description = "특정 강사의 상세 정보 조회")
    public ResponseEntity<InstructorResponseDto> getInstructorById(
            @PathVariable Long instructorId) {
        InstructorResponseDto instructor = instructorService.getInstructorById(instructorId);
        return ResponseEntity.ok(instructor);
    }
}