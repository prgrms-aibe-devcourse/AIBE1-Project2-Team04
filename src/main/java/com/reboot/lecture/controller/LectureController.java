package com.reboot.lecture.controller;

import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Hidden // Swagger에서 숨김
// 강의 관련 요청을 처리 (강의 목록 조회, 검색, 상세 페이지 등)
@Tag(name = "강의 API", description = "강의 조회, 검색, 필터링 API")
@Controller
@RequestMapping("/lectures") // 기존 경로 유지
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    // === 웹 페이지 반환 메소드 (View) ===
    // 전체 강의 목록 페이지 (추가)
    @GetMapping  // 루트 경로: /lectures
    public String lecturesList(
            @RequestParam(required = false) String game,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 페이징 정보 설정
        Pageable pageable = PageRequest.of(page, 30);

        // 게임 타입 목록 조회 (네비게이션 메뉴용)
        List<String> gameTypes = lectureService.getAllActiveGameTypes();
        model.addAttribute("gameTypes", gameTypes);

        // 게임 타입이 지정된 경우
        if (game != null && !game.isEmpty()) {
            // 해당 게임 타입의 강의 목록을 가져옴
            Page<LectureResponseDto> lectures = lectureService.getLecturesByGameType(game, sortBy, pageable);
            model.addAttribute("lectures", lectures);
            model.addAttribute("currentGameType", game);
        } else {
            // 전체 강의 목록을 가져옴
            Page<LectureResponseDto> lectures = lectureService.getAllActiveLectures(pageable);
            model.addAttribute("lectures", lectures);
        }

        model.addAttribute("currentSortBy", sortBy);

        // lectures/index.html 템플릿 반환
        return "lectures/index";
    }

    // 홈 화면 (메인 페이지) - 인기 강의 목록 표시
    @GetMapping("/home") // /api/lectures/home
    public String home(Model model) {
        // 인기 강의 목록 조회 (인기순 정렬)
        List<LectureResponseDto> popularLectures = lectureService.getActivePopularLectures();
        // 활성화된 모든 게임 타입 목록 조회 (네비게이션 메뉴)
        List<String> gameTypes = lectureService.getAllActiveGameTypes();

        model.addAttribute("lectures", popularLectures); // 강의 목록
        model.addAttribute("gameTypes", gameTypes); // 게임 타입 목록

        return "lectures/home";
    }

    // 게임 타입(장르) 별 강의 목록 페이지
    @GetMapping("/game/{gameType}") // /api/lectures/game/{gameType}
    public String lecturesByGameType(
            @PathVariable String gameType,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(required = false) String rank,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 페이징 정보 설정 (한 페이지에 30개 항목)
        Pageable pageable = PageRequest.of(page, 30);

        Page<LectureResponseDto> lectures;
        if (rank != null || position != null) {
            // 필터가 적용된 경우 (랭크 또는 포지션)
            lectures = lectureService.getFilteredLectures(gameType, rank, position, sortBy, pageable);
        } else {
            // 기본 정렬만 적용된 경우
            lectures = lectureService.getLecturesByGameType(gameType, sortBy, pageable);
        }

        // 게임 타입 목록 조회 (네비게이션 메뉴)
        List<String> gameTypes = lectureService.getAllActiveGameTypes();

        model.addAttribute("lectures", lectures); // 강의 페이지 객체
        model.addAttribute("gameTypes", gameTypes); // 게임 타입 목록
        model.addAttribute("currentGameType", gameType); // 현재 선택된 게임 타입
        model.addAttribute("currentSortBy", sortBy); // 현재 적용된 정렬 기준
        model.addAttribute("currentRank", rank); // 현재 적용된 랭크 필터
        model.addAttribute("currentPosition", position); // 현재 적용된 포지션 필터

        return "lectures/game-lectures";
    }

    // 강의 키워드 검색 결과 페이지
    @GetMapping("/search") // /api/lectures/search
    public String searchLectures(
            @RequestParam String keyword,
            @RequestParam(required = false) String gameType,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 페이징 정보 설정 (한 페이지에 30개 항목)
        Pageable pageable = PageRequest.of(page, 30);
        Page<LectureResponseDto> searchResults;

        // 게임 타입이 지정된 경우와 전체 검색의 경우를 구분하여 처리
        if (gameType != null && !gameType.isEmpty()) {
            // 특정 게임 타입 내에서 검색
            searchResults = lectureService.searchLecturesByGameType(gameType, keyword, pageable);
            model.addAttribute("currentGameType", gameType); // 현재 선택된 게임 타입
        } else {
            // 전체 강의에서 검색
            searchResults = lectureService.searchLectures(keyword, pageable);
        }

        // 게임 타입 목록 조회 (네비게이션 메뉴)
        List<String> gameTypes = lectureService.getAllActiveGameTypes();

        model.addAttribute("lectures", searchResults); // 검색 결과 페이지 객체
        model.addAttribute("gameTypes", gameTypes); // 게임 타입 목록
        model.addAttribute("keyword", keyword); // 검색 키워드 (검색창에 유지)

        return "lectures/search-results";
    }

    // 강의 상세 페이지 (특정 ID의 강의 상세 정보 표시)
    @GetMapping("/{id}") // /api/lectures/{id}
    public String lectureDetail(
            @PathVariable Long id,
            Model model) {
        // ID로 강의 상세 정보 조회 (없으면 LectureNotFoundException 발생)
        LectureResponseDto lecture = lectureService.getLectureById(id);
        model.addAttribute("lecture", lecture);

        return "lectures/detail";
    }

    // === REST API 메소드 (JSON 반환) ===

    @GetMapping("/popular")
    @ResponseBody
    @Operation(summary = "인기 강의 목록 조회", description = "인기순으로 정렬된 활성화된 강의 목록 조회")
    public List<LectureResponseDto> getActivePopularLectures() {
        return lectureService.getActivePopularLectures();
    }

/*
    @GetMapping
    @ResponseBody
    @Operation(summary = "전체 강의 목록 조회", description = "페이징 처리된 모든 활성화된 강의 목록 조회")
    public Page<LectureResponseDto> getAllActiveLectures(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.getAllActiveLectures(pageable);
    }
*/

    @GetMapping("/lecture-list/game/{gameType}")
    @ResponseBody
    @Operation(summary = "게임별 강의 목록 조회", description = "특정 게임 타입에 대한 강의 목록 조회 (정렬 옵션 적용)")
    public Page<LectureResponseDto> getLecturesByGameType(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)") @PathVariable String gameType,
            @Parameter(description = "정렬 기준 (인기순, 최신순, 낮은 가격순, 높은 가격순)")
            @RequestParam(defaultValue = "rating") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(강의 개수)") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.getLecturesByGameType(gameType, sortBy, pageable);
    }

    @GetMapping("/filtered")
    @ResponseBody
    @Operation(summary = "필터링된 강의 목록 조회", description = "게임 타입, 랭크, 포지션 등으로 필터링된 강의 목록 조회")
    public Page<LectureResponseDto> getFilteredLectures(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)") @RequestParam String gameType,
            @Parameter(description = "랭크 (예: bronze, silver, gold)") @RequestParam(required = false) String lectureRank,
            @Parameter(description = "포지션 (예: top, jungle, mid)") @RequestParam(required = false) String position,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "rating") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(강의 개수)") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.getFilteredLectures(gameType, lectureRank, position, sortBy, pageable);
    }

    @GetMapping("/search-api")
    @ResponseBody
    @Operation(summary = "강의 검색", description = "키워드로 강의 검색 (제목, 설명에서 검색)")
    public Page<LectureResponseDto> searchLecturesApi(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(강의 개수)") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.searchLectures(keyword, pageable);
    }

    @GetMapping("/search-api/game/{gameType}")
    @ResponseBody
    @Operation(summary = "게임별 강의 검색", description = "특정 게임 타입 내에서 키워드로 강의 검색")
    public Page<LectureResponseDto> searchLecturesByGameTypeApi(
            @Parameter(description = "게임 타입 (예: LOL, VALORANT)") @PathVariable String gameType,
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lectureService.searchLecturesByGameType(gameType, keyword, pageable);
    }

    @GetMapping("/game-types")
    @ResponseBody
    @Operation(summary = "게임 타입 목록 조회", description = "활성화된 모든 게임 타입(장르) 목록 조회")
    public List<String> getAllActiveGameTypes() {
        return lectureService.getAllActiveGameTypes();
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    @Operation(summary = "강의 상세 정보 조회", description = "특정 ID의 강의 상세 정보 조회")
    public LectureResponseDto getLectureByIdApi(
            @Parameter(description = "강의 ID") @PathVariable Long id) {
        return lectureService.getLectureById(id);
    }
}