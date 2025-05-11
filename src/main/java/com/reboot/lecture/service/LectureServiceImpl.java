package com.reboot.lecture.service;

import com.reboot.lecture.dto.LectureResponseDto;
import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.exception.LectureNotFoundException;
import com.reboot.lecture.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 강의 조회, 필터링, 검색하는 비즈니스 로직 처리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;

    // 홈 화면에 표시할 강의 목록 조회 (인기순 - 디폴트)
    @Override
    public List<LectureResponseDto> getActivePopularLectures() {
        List<Lecture> lectures = lectureRepository.findByOrderByPopularity();
        return LectureResponseDto.fromEntities(lectures);
    }

    // 모든 강의 조회
    @Override
    public Page<LectureResponseDto> getAllActiveLectures(Pageable pageable) {
        Page<Lecture> lecturePage = lectureRepository.findAll(pageable);
        return lecturePage.map(LectureResponseDto::fromEntity);
    }

    @Override
    public Page<LectureResponseDto> getLecturesByGameType(String gameType, String sortBy, Pageable pageable) {
        Page<Lecture> lecturePage;

        // 정렬 기준에 따라 적절한 리포지토리 메서드 호출
        switch (sortBy) {
            case "newest": // 최신순
                lecturePage = lectureRepository.findByGameTypeOrderByCreatedAtDesc(gameType, pageable);
                break;
            case "priceLow": // 가격 낮은순
                lecturePage = lectureRepository.findByGameTypeOrderByPriceAsc(gameType, pageable);
                break;
            case "priceHigh": // 가격 높은순
                lecturePage = lectureRepository.findByGameTypeOrderByPriceDesc(gameType, pageable);
                break;
            case "rating": // 별점순 정렬 (기본)
            default:
                lecturePage = lectureRepository.findByGameTypeOrderByAverageRatingDesc(gameType, pageable);
                break;
        }
        return lecturePage.map(LectureResponseDto::fromEntity);
    }

    // 필터링된 강의 목록 조회(게임 타입(장르), 랭크(티어), 포지션 등)
    @Override
    public Page<LectureResponseDto> getFilteredLectures(String gameType, String lectureRank,
                                                        String position, String sortBy, Pageable pageable) {
        Page<Lecture> lecturePage = lectureRepository.findByGameTypeWithFiltersAndSort(
                gameType, lectureRank, position, sortBy, pageable);
        return lecturePage.map(LectureResponseDto::fromEntity);
    }

    // 제목 or 설명에 포함된 키워드로 강의 검색
    @Override
    public Page<LectureResponseDto> searchLectures(String keyword, Pageable pageable) {
        Page<Lecture> lecturePage = lectureRepository
                .findByTitleOrDescriptionContaining(keyword, pageable);
        return lecturePage.map(LectureResponseDto::fromEntity);
    }

    // 게임 타입(장르) 내에서 키워드로 강의 검색(제목 or 설명에 포함된 키워드)
    @Override
    public Page<LectureResponseDto> searchLecturesByGameType(String gameType, String keyword, Pageable pageable) {
        Page<Lecture> lecturePage = lectureRepository
                .findByTitleOrDescriptionContainingAndGameType(gameType, keyword, pageable);
        return lecturePage.map(LectureResponseDto::fromEntity);
    }

    // 모든 게임 타입 목록 조회
    @Override
    public List<String> getAllActiveGameTypes() {
        return lectureRepository.findAllActiveGameTypes();
    }

    // 특정 강의 ID의 상세 정보 조회
    @Override
    public LectureResponseDto getLectureById(Long id) {
        // 강사 정보를 함께 로딩하여 N+1 문제를 방지
        return lectureRepository.findByIdWithInstructor(id)
                .map(LectureResponseDto::fromEntity)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + id));
    }

    @Override
    public Lecture getLecture(Long id) {
        return lectureRepository.findByIdWithInstructor(id)
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다: " + id));
    }
}