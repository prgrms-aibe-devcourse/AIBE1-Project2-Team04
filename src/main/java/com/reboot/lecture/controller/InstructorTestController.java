package com.reboot.lecture.controller;

import com.reboot.auth.dto.InstructorResponseDto;
import com.reboot.auth.service.InstructorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 강사 조회, 검색, 필터링 테스트 (Swagger)
@RestController
@RequestMapping("/api/test/instructors")
@RequiredArgsConstructor
@Tag(name = "강사 테스트 API", description = "강사 조회, 검색, 필터링 테스트용 API")
public class InstructorTestController {

    private final InstructorService instructorService;

    // 모든 강사 목록 조회 (정렬 옵션 적용)
    @GetMapping
    @Operation(summary = "테스트: 강사 목록 조회", description = "정렬 옵션이 적용된 강사 목록 조회")
    public Page<InstructorResponseDto> getAllInstructors(
            @Parameter(description = "정렬 기준 (popularity, newest, reviews, students)")
            @RequestParam(defaultValue = "popularity") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return instructorService.getAllInstructors(sortBy, pageable);
    }

    // 특정 게임 타입에 전문성을 가진 강사 목록 조회
    @GetMapping("/game/{gameType}")
    @Operation(summary = "테스트: 게임별 강사 목록 조회", description = "특정 게임 타입에 전문성을 가진 강사 목록 조회")
    public List<InstructorResponseDto> getInstructorsByGameType(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)")
            @PathVariable String gameType) {

        return instructorService.getInstructorsByGameType(gameType);
    }

    // 특정 포지션에 전문성을 가진 강사 목록 조회
    @GetMapping("/position/{position}")
    @Operation(summary = "테스트: 포지션별 강사 목록 조회", description = "특정 포지션에 전문성을 가진 강사 목록 조회")
    public List<InstructorResponseDto> getInstructorsByPosition(
            @Parameter(description = "포지션 (예: top, jungle, mid)")
            @PathVariable String position) {

        return instructorService.getInstructorsByPosition(position);
    }

    // 특정 티어에 해당하는 강사 목록 조회 (추가)
    @GetMapping("/tier/{gameTier}")
    @Operation(summary = "테스트: 티어별 강사 목록 조회", description = "특정 티어에 해당하는 강사 목록 조회")
    public List<InstructorResponseDto> getInstructorsByGameTier(
            @Parameter(description = "게임 티어 (예: bronze, silver, gold)")
            @PathVariable String gameTier) {

        return instructorService.getInstructorsByGameTier(gameTier);
    }

    // 페이징 지원 티어별 강사 목록 조회 (추가)
    @GetMapping("/tier/{gameTier}/paging")
    @Operation(summary = "테스트: 페이징 지원 티어별 강사 목록 조회", description = "페이징이 적용된 티어별 강사 목록 조회")
    public Page<InstructorResponseDto> getInstructorsByGameTierWithPaging(
            @Parameter(description = "게임 티어 (예: bronze, silver, gold)")
            @PathVariable String gameTier,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return instructorService.getInstructorsByGameTierWithPaging(gameTier, pageable);
    }

    // 강사 검색 (닉네임 또는 소개글 기준)
    @GetMapping("/search")
    @Operation(summary = "테스트: 강사 검색", description = "닉네임 또는 소개글에 특정 키워드가 포함된 강사 검색")
    public List<InstructorResponseDto> searchInstructors(
            @Parameter(description = "검색 키워드")
            @RequestParam String keyword) {

        return instructorService.searchInstructors(keyword);
    }

    // 페이징 지원 강사 검색
    @GetMapping("/search/paging")
    @Operation(summary = "테스트: 페이징 지원 강사 검색", description = "페이징이 적용된 강사 검색")
    public Page<InstructorResponseDto> searchInstructorsWithPaging(
            @Parameter(description = "검색 키워드")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return instructorService.searchInstructorsWithPaging(keyword, pageable);
    }

    // 강사 상세 정보 조회
    @GetMapping("/{instructorId}")
    @Operation(summary = "테스트: 강사 상세 정보 조회", description = "특정 강사의 상세 정보 조회")
    public InstructorResponseDto getInstructorById(
            @Parameter(description = "강사 ID")
            @PathVariable Long instructorId) {

        return instructorService.getInstructorById(instructorId);
    }

    // 게임 티어 목록 조회 (추가)
    @GetMapping("/game-tiers")
    @Operation(summary = "테스트: 게임 티어 목록 조회", description = "사용 가능한 모든 게임 티어 목록 조회")
    public Object getGameTiers(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)")
            @RequestParam(required = false) String gameType) {

        // 간단한 응답 객체를 만들어 반환
        return new Object() {
            public final String[] tiers = gameType != null && gameType.equalsIgnoreCase("LOL")
                    ? new String[]{"iron", "bronze", "silver", "gold", "platinum", "diamond", "master", "grandmaster", "challenger"}
                    : gameType != null && gameType.equalsIgnoreCase("VALORANT")
                    ? new String[]{"iron", "bronze", "silver", "gold", "platinum", "diamond", "ascendant", "immortal", "radiant"}
                    : new String[]{"beginner", "intermediate", "advanced", "expert"};
        };
    }
}