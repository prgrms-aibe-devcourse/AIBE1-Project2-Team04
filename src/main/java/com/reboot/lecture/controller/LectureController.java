package com.reboot.lecture.controller;

import com.reboot.lecture.dto.LectureResponse;
import com.reboot.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 강의 관련 요청을 처리 (강의 목록 조회, 검색, 상세 페이지 등)
// 강의 관련 뷰를 제공하는 엔드포인트 정의
@Tag(name = "강의 API", description = "강의 조회, 검색, 필터링 API")
@RestController
@Controller
@RequestMapping("/api/lectures") // 기본 경로
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;


    // 홈 화면 (메인 페이지) - 인기 강의 목록 표시
    @GetMapping("/home") // /lectures/home
    public String home(Model model) {
        // 인기 강의 목록 조회 (인기순 정렬)
        List<LectureResponse> popularLectures = lectureService.getActivePopularLectures();
        // 활성화된 모든 게임 타입 목록 조회 (네비게이션 메뉴)
        List<String> gameTypes = lectureService.getAllActiveGameTypes();

        model.addAttribute("lectures", popularLectures); // 강의 목록
        model.addAttribute("gameTypes", gameTypes); // 게임 타입 목록

        return "lectures/home";
    }


    // 게임 타입(장르) 별 강의 목록 페이지
    // 특정 게임 타입에 대한 강의 목록 표시 ( ex) 롤, 발로란트 등)
    // 정렬 옵션과 필터(랭크, 포지션) 적용 가능
    @GetMapping("/game/{gameType}") // /lectures/game/{gameType}
    public String lecturesByGameType(
            @PathVariable String gameType,
            @RequestParam(defaultValue = "popularity") String sortBy,
            @RequestParam(required = false) String rank,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 페이징 정보 설정 (한 페이지에 30개 항목)
        Pageable pageable = PageRequest.of(page, 30);

        Page<LectureResponse> lectures;
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

        // 'lectures/game-lectures' 뷰 템플릿 경로 반환
        return "lectures/game-lectures";
    }


    // 강의 키워드 검색 결과 페이지
    // 사용자가 입력한 키워드로 강의를 검색
    // 선택적으로 특정 게임 타입 내에서만 검색할 수도 있습니다
    @GetMapping("/search") // /lectures/search
    public String searchLectures(
            @RequestParam String keyword,
            @RequestParam(required = false) String gameType,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 페이징 정보 설정 (한 페이지에 30개 항목)
        Pageable pageable = PageRequest.of(page, 30);
        Page<LectureResponse> searchResults;

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

        // 'lectures/search-results' 뷰 템플릿 경로 반환
        return "lectures/search-results";
    }


    // 강의 상세 페이지 (특정 ID의 강의 상세 정보 표시)
    // 강의 세부 내용, 강사 정보, 리뷰 등이 포함된 상세 페이지 구성
    // 이 페이지에서 수강신청이나 결제로 이어질 수 있는 링크 제공 (생각만 함.. 의선님 감사합니다..)
    // ID에 해당하는 강의가 없는 경우 LectureNotFoundException 발생
    @GetMapping("/{id}") // /lectures/{id}
    public String lectureDetail(
            @PathVariable String id,
            Model model) {
        // ID로 강의 상세 정보 조회 (없으면 LectureNotFoundException 발생)
        LectureResponse lecture = lectureService.getLectureById(id);
        model.addAttribute("lecture", lecture);

        // 'lectures/detail' 뷰 템플릿 경로 반환
        return "lectures/detail";
    }
}