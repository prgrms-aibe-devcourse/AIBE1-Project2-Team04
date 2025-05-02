package com.reboot.lecture.service;

import com.reboot.lecture.dto.LectureResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// 강의 관련 비즈니스 로직(강의 조회, 필터링, 검색 등) 처리
// 구현체(Impl) 별도
public interface LectureService {


    // 홈 화면에 표시할 활성화된 강의 목록 조회 (인기순)
    List<LectureResponse> getActivePopularLectures();


    // 모든 활성화된 강의 조회
    // "더보기" 기능 등에서 사용
    Page<LectureResponse> getAllActiveLectures(Pageable pageable);


    // 게임 타입별 강의 목록 조회 (다양한 정렬 옵션)
    Page<LectureResponse> getLecturesByGameType(String gameType, String sortBy, Pageable pageable);


    // 필터링된 강의 목록 조회
    Page<LectureResponse> getFilteredLectures(String gameType, String lectureRank, String position, String sortBy, Pageable pageable);


    // 홈에서 키워드로 강의 검색
    Page<LectureResponse> searchLectures(String keyword, Pageable pageable);


    // 선택한 게임 타입(장르) 내에서 키워드로 강의 검색
    Page<LectureResponse> searchLecturesByGameType(String gameType, String keyword, Pageable pageable);


    // 모든 활성 게임 타입 조회
    List<String> getAllActiveGameTypes();


    // 특정 ID의 강의 상세 정보 조회
    // 강의 상세 페이지에서 특정 강의의 모든 정보를 조회할 때 사용
    // 강사 정보를 함께 로딩하여 N+1 문제를 방지
    LectureResponse getLectureById(String id);
}