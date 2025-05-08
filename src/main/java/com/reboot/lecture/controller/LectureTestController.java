package com.reboot.lecture.controller;

import com.reboot.lecture.dto.LectureResponse;
import com.reboot.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 강의 조회, 검색, 필터링 테스트 (Swagger)
@RestController
@RequestMapping("/api/test/lectures")
@RequiredArgsConstructor
@Tag(name = "강의 테스트 API", description = "강의 조회, 검색, 필터링 테스트용 API")
public class LectureTestController {

    private final LectureService lectureService;

    @GetMapping("/popular")
    @Operation(summary = "테스트: 인기 강의 목록 조회", description = "인기순으로 정렬된 활성화된 강의 목록 조회")
    public List<LectureResponse> getActivePopularLectures() {
        return lectureService.getActivePopularLectures();
    }

    @GetMapping
    @Operation(summary = "테스트: 전체 강의 목록 조회", description = "페이징 처리된 모든 활성화된 강의 목록 조회")
    public Page<LectureResponse> getAllActiveLectures(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.getAllActiveLectures(pageable);
    }

    @GetMapping("/game/{gameType}")
    @Operation(summary = "테스트: 게임별 강의 목록 조회", description = "특정 게임 타입에 대한 강의 목록 조회 (정렬 옵션 적용)")
    public Page<LectureResponse> getLecturesByGameType(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)") @PathVariable String gameType,
            @Parameter(description = "정렬 기준 (인기순, 최신순, 리뷰순, 낮은 가격순, 높은 가격순)")
            @RequestParam(defaultValue = "popularity") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(강의 개수)") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.getLecturesByGameType(gameType, sortBy, pageable);
    }

    @GetMapping("/filtered")
    @Operation(summary = "테스트: 필터링된 강의 목록 조회", description = "게임 타입, 랭크, 포지션 등으로 필터링된 강의 목록 조회")
    public Page<LectureResponse> getFilteredLectures(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)") @RequestParam String gameType,
            @Parameter(description = "랭크 (예: bronze, silver, gold)") @RequestParam(required = false) String lectureRank,
            @Parameter(description = "포지션 (예: top, jungle, mid)") @RequestParam(required = false) String position,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "popularity") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(강의 개수)") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.getFilteredLectures(gameType, lectureRank, position, sortBy, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "테스트: 강의 검색", description = "키워드로 강의 검색 (제목, 설명에서 검색)")
    public Page<LectureResponse> searchLectures(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(강의 개수)") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.searchLectures(keyword, pageable);
    }

    @GetMapping("/search/game/{gameType}")
    @Operation(summary = "테스트: 게임별 강의 검색", description = "특정 게임 타입 내에서 키워드로 강의 검색")
    public Page<LectureResponse> searchLecturesByGameType(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)") @PathVariable String gameType,
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.searchLecturesByGameType(gameType, keyword, pageable);
    }

    @GetMapping("/game-types")
    @Operation(summary = "테스트: 게임 타입 목록 조회", description = "활성화된 모든 게임 타입(장르) 목록 조회")
    public List<String> getAllActiveGameTypes() {
        return lectureService.getAllActiveGameTypes();
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "테스트: 강의 상세 정보 조회", description = "특정 ID의 강의 상세 정보 조회")
    public LectureResponse getLectureById(
            @Parameter(description = "강의 ID") @PathVariable Long id) {
        return lectureService.getLectureById(id);
    }
}