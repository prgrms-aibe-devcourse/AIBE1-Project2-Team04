package com.reboot.lecture.repository;

import com.reboot.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


// 강의 정보 조회 리포지토리 인터페이스
// 다양한 필터링과 정렬 옵션 제공
@Repository
public interface LectureRepository extends JpaRepository<Lecture, String> {

    // 인기도 계산 공식 상수 정의 (재사용성, 일관성)
    // 인기도 계산: 별점 + (리뷰 수/100.0) + (총 수강생 수/100.0) -> 소수점 2자리까지 계산
    String POPULARITY_FORMULA = "(l.averageRating + (l.reviewCount/100.0) + (l.totalMembers/100.0))";


    // 홈 화면 - 인기순 정렬(디폴트)
    @Query("SELECT l FROM Lecture l WHERE l.isActive = true ORDER BY " + POPULARITY_FORMULA + " DESC")
    List<Lecture> findByIsActiveTrueOrderByPopularity();


    // 게임별 강의 목록 - 인기순 정렬(디폴트)
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = true ORDER BY " + POPULARITY_FORMULA + " DESC")
    Page<Lecture> findByGameTypeAndIsActiveTrueOrderByPopularity(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 최신순 정렬
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = true ORDER BY l.createdAt DESC")
    Page<Lecture> findByGameTypeAndIsActiveTrueOrderByCreatedAtDesc(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 리뷰 많은순 정렬(내림차순)
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = true ORDER BY l.reviewCount DESC")
    Page<Lecture> findByGameTypeAndIsActiveTrueOrderByReviewCountDesc(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 가격 낮은순 정렬(오름차순)
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = true ORDER BY l.price ASC")
    Page<Lecture> findByGameTypeAndIsActiveTrueOrderByPriceAsc(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 가격 높은순 정렬(내림차순)
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = true ORDER BY l.price DESC")
    Page<Lecture> findByGameTypeAndIsActiveTrueOrderByPriceDesc(@Param("gameType") String gameType, Pageable pageable);


    // 활성화된 강의 조회(전체)
    // "더보기" 버튼 클릭 시 활성화된 전체 강의를 페이징 처리해서 보여주기 위함
    Page<Lecture> findByIsActiveTrue(Pageable pageable);


    // 게임 타입(장르)별 강의 조회
    Page<Lecture> findByGameTypeAndIsActiveTrue(String gameType, Pageable pageable);


    // 가격 범위로 강의 조회
    Page<Lecture> findByPriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);


    // 제목 또는 설명에 특정 키워드가 포함된 강의 검색
    // 홈 화면 검색 기능 구현을 위한 메서드
    // 공백으로 구분된 복수 키워드 검색 지원
    @Query(value = "SELECT DISTINCT * FROM lecture l " +
            "WHERE l.is_active = true " +
            "AND (:keyword IS NULL OR " +
            "REGEXP_LIKE(LOWER(l.title), LOWER(REPLACE(:keyword, ' ', '|'))) " +
            "OR REGEXP_LIKE(LOWER(l.description), LOWER(REPLACE(:keyword, ' ', '|'))))",
            nativeQuery = true)
    Page<Lecture> findByTitleOrDescriptionContainingAndIsActiveTrue(
            @Param("keyword") String keyword,
            Pageable pageable);


    // 게임별 강의 키워드 검색 (제목 또는 설명)
    // 게임 카테고리 내에서 검색 기능 구현을 위한 메서드
    @Query(value = "SELECT DISTINCT * FROM lecture l " +
            "WHERE l.is_active = true AND l.game_type = :gameType " +
            "AND (:keyword IS NULL OR " +
            "REGEXP_LIKE(LOWER(l.title), LOWER(REPLACE(:keyword, ' ' '|'))) " +
            "OR REGEXP_LIKE(LOWER(l.description), LOWER(REPLACE(:keyword, ' ', '|'))))",
            nativeQuery = true)
    Page<Lecture> findByTitleOrDescriptionContainingAndGameTypeAndIsActiveTrue(
            @Param("gameType") String gameType,
            @Param("keyword") String keyword,
            Pageable pageable);


    // 동적 필터링 (랭크, 포지션)과 정렬 옵션(인기순, 최신순, 리뷰수, 가격순)
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = true " +
            "AND (:lectureRank IS NULL OR l.lectureRank = :lectureRank) " +
            "AND (:position IS NULL OR l.position = :position) " +
            "ORDER BY " +
            "CASE " +
            "  WHEN :sortBy = 'popularity' THEN " + POPULARITY_FORMULA + " " +
            "  WHEN :sortBy = 'newest' THEN 0 " +
            "  WHEN :sortBy = 'reviews' THEN l.reviewCount " +
            "  WHEN :sortBy = 'priceHigh' THEN l.price " +
            "  ELSE 0 " +
            "END DESC, " +
            "CASE " +
            "  WHEN :sortBy = 'newest' THEN l.createdAt " +
            "  ELSE NULL " +
            "END DESC, " +
            "CASE " +
            "  WHEN :sortBy = 'priceLow' THEN l.price " +
            "  ELSE NULL " +
            "END ASC")
    Page<Lecture> findByGameTypeWithFiltersAndSort(
            @Param("gameType") String gameType,
            @Param("lectureRank") String lectureRank,
            @Param("position") String position,
            @Param("sortBy") String sortBy,
            Pageable pageable);


    // 게임 카테고리 목록 조회
    @Query("SELECT DISTINCT l.gameType FROM Lecture l WHERE l.isActive = true ORDER BY l.gameType")
    List<String> findAllActiveGameTypes();


     // 강의 상세 조회 시 instructor 정보를 함께 로딩 (N+1 문제 해결)
     // 강의 ID
     // Instructor 정보가 함께 로드된 강의 객체
    @Query("SELECT l FROM Lecture l JOIN FETCH l.instructor WHERE l.id = :id AND l.isActive = true")
    Lecture findByIdWithInstructor(@Param("id") Long id);





    // <<<<<<<<<<  강의 CRUD 관련 추가 메서드  >>>>>>>>>>



    // 강사 ID로 강의 목록 조회 (삭제되지 않은 강의)
    // 특정 강사가 생성한 모든 강의를 조회하기 위한 메서드
    List<Lecture> findByInstructorInstructorIdAndDeletedAtIsNull(Long instructorId);


    // 강의 ID와 강사 ID로 강의 조회
    // 특정 강사의 특정 강의를 조회하기 위한 메서드
    @Query("SELECT l FROM Lecture l WHERE l.id = :lectureId AND l.instructor.instructorId = :instructorId AND l.deletedAt IS NULL")
    Lecture findByIdAndInstructorId(@Param("lectureId") Long lectureId, @Param("instructorId") Long instructorId);




    // 추가: 강사 ID로 첫 번째 강의 조회
    Optional<Lecture> findFirstByInstructorInstructorId(Long instructorId);
}