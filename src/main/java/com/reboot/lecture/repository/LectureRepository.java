package com.reboot.lecture.repository;

import com.reboot.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


// 강의 정보 조회 리포지토리 인터페이스
// 다양한 필터링과 정렬 옵션 제공
@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> { // ID 타입을 Long으로 변경

    // 인기도 계산 공식 상수 정의 (재사용성, 일관성)
    // 인기도 계산: 별점 + (리뷰 수/100.0) + (총 수강생 수/100.0) -> 소수점 2자리까지 계산
    String POPULARITY_FORMULA = "(l.metadata.averageRating + (l.metadata.reviewCount/100.0) + (l.metadata.totalMembers/100.0))";


    // 홈 화면 - 인기순 정렬(디폴트)
    @Query("SELECT l FROM Lecture l ORDER BY " + POPULARITY_FORMULA + " DESC")
    List<Lecture> findByOrderByPopularity();


    // 게임별 강의 목록 - 인기순 정렬(디폴트)
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType ORDER BY " + POPULARITY_FORMULA + " DESC")
    Page<Lecture> findByGameTypeOrderByPopularity(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 최신순 정렬
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType ORDER BY l.metadata.createdAt DESC")
    Page<Lecture> findByGameTypeOrderByCreatedAtDesc(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 리뷰 많은순 정렬(내림차순)
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType ORDER BY l.metadata.reviewCount DESC")
    Page<Lecture> findByGameTypeOrderByReviewCountDesc(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 가격 낮은순 정렬(오름차순)
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType ORDER BY l.info.price ASC")
    Page<Lecture> findByGameTypeOrderByPriceAsc(@Param("gameType") String gameType, Pageable pageable);


    // 게임별 강의 목록 - 가격 높은순 정렬(내림차순)
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType ORDER BY l.info.price DESC")
    Page<Lecture> findByGameTypeOrderByPriceDesc(@Param("gameType") String gameType, Pageable pageable);


    // 활성화된 강의 조회(전체)
    Page<Lecture> findAll(Pageable pageable);


    // 게임 타입(장르)별 강의 조회
    Page<Lecture> findByInfoGameType(String gameType, Pageable pageable);


    // 가격 범위로 강의 조회
    Page<Lecture> findByInfoPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // 게임별 강의 목록 - 별점순 정렬(내림차순)
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType ORDER BY l.metadata.averageRating DESC")
    Page<Lecture> findByGameTypeOrderByAverageRatingDesc(@Param("gameType") String gameType, Pageable pageable);

    // 제목 또는 설명에 특정 키워드가 포함된 강의 검색
    // 홈 화면 검색 기능 구현을 위한 메서드
    // 공백으로 구분된 복수 키워드 검색 지원
    @Query("SELECT l FROM Lecture l WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(l.info.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.info.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Lecture> findByTitleOrDescriptionContaining(
            @Param("keyword") String keyword,
            Pageable pageable);


    // 게임별 강의 키워드 검색 (제목 또는 설명)
    // 게임 카테고리 내에서 검색 기능 구현을 위한 메서드
    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType AND " +
            "(:keyword IS NULL OR " +
            "LOWER(l.info.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.info.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Lecture> findByTitleOrDescriptionContainingAndGameType(
            @Param("gameType") String gameType,
            @Param("keyword") String keyword,
            Pageable pageable);


    @Query("SELECT l FROM Lecture l WHERE l.info.gameType = :gameType " +
            "AND (:lectureRank IS NULL OR l.info.lectureRank = :lectureRank) " +
            "AND (:position IS NULL OR l.info.position = :position) " +
            "ORDER BY " +
            "CASE " +
            "  WHEN :sortBy = 'rating' THEN l.metadata.averageRating " +  // 변경: popularity -> rating
            "  WHEN :sortBy = 'priceHigh' THEN l.info.price " +
            "  WHEN :sortBy = 'priceLow' THEN 0 " + // 가격 낮은순은 ASC로 정렬해야 함
            "  WHEN :sortBy = 'newest' THEN 0 " +
            "  ELSE l.metadata.averageRating " + // 기본값도 별점순으로 변경
            "END DESC, " +
            "CASE " +
            "  WHEN :sortBy = 'newest' THEN l.metadata.createdAt " +
            "  ELSE NULL " +
            "END DESC, " +
            "CASE " +
            "  WHEN :sortBy = 'priceLow' THEN l.info.price " +
            "  ELSE NULL " +
            "END ASC")
    Page<Lecture> findByGameTypeWithFiltersAndSort(
            @Param("gameType") String gameType,
            @Param("lectureRank") String lectureRank,
            @Param("position") String position,
            @Param("sortBy") String sortBy,
            Pageable pageable);

    // 게임 카테고리 목록 조회
    @Query("SELECT DISTINCT l.info.gameType FROM Lecture l ORDER BY l.info.gameType")
    List<String> findAllActiveGameTypes();


    // 강의 상세 조회 시 instructor 정보를 함께 로딩 (N+1 문제 해결)
    @Query("SELECT l FROM Lecture l JOIN FETCH l.instructor WHERE l.id = :id")
    Optional<Lecture> findByIdWithInstructor(@Param("id") Long id);

    // 새로 추가된 메소드 - 모든 연관 엔티티 함께 로딩
    @Query("SELECT l FROM Lecture l " +
            "LEFT JOIN FETCH l.instructor i " +
            "LEFT JOIN FETCH i.member " +
            "LEFT JOIN FETCH l.lectureDetail " +
            "WHERE l.id = :id")
    Optional<Lecture> findByIdWithFullDetails(@Param("id") Long id);


    // 강사 ID로 강의 목록 조회
    List<Lecture> findByInstructorInstructorId(Long instructorId);

    // 정렬 지원 메소드 추가
    List<Lecture> findByInstructorInstructorId(Long instructorId, Sort sort);

    // 수정 - metadata.createdAt을 사용
    List<Lecture> findByInstructorInstructorIdOrderByMetadataCreatedAtDesc(Long instructorId);


    // 강의 ID와 강사 ID로 강의 조회
    @Query("SELECT l FROM Lecture l WHERE l.id = :lectureId AND l.instructor.instructorId = :instructorId")
    Optional<Lecture> findByIdAndInstructorInstructorId(
            @Param("lectureId") Long lectureId,
            @Param("instructorId") Long instructorId);


    // 추가: 강사 ID로 첫 번째 강의 조회
    Optional<Lecture> findFirstByInstructorInstructorId(Long instructorId);
}