package com.reboot.lecture.repository;

import com.reboot.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    
    /**
     * 게임 타입과 활성 상태에 따라 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndActive(@Param("gameType") String gameType, @Param("isActive") boolean isActive);
    
    /**
     * 게임 타입, 포지션, 활성 상태에 따라 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.position = :position AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndPositionAndActive(
            @Param("gameType") String gameType, 
            @Param("position") String position, 
            @Param("isActive") boolean isActive);
    
    /**
     * 게임 타입, 티어/랭크, 활성 상태에 따라 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.rank_ LIKE %:rank% AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndRankContainingAndActive(
            @Param("gameType") String gameType, 
            @Param("rank") String rank, 
            @Param("isActive") boolean isActive);
    
    /**
     * 게임 타입, 포지션, 티어/랭크, 활성 상태에 따라 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.position = :position AND l.rank_ LIKE %:rank% AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndPositionAndRankContainingAndActive(
            @Param("gameType") String gameType, 
            @Param("position") String position, 
            @Param("rank") String rank, 
            @Param("isActive") boolean isActive);
    
    /**
     * 강사 ID와 활성 상태에 따라 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.instructor.instructorId = :instructorId AND l.isActive = :isActive")
    List<Lecture> findByInstructorIdAndActive(
            @Param("instructorId") Long instructorId, 
            @Param("isActive") boolean isActive);
    
    /**
     * 게임 타입과 강의 제목 키워드로 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND UPPER(l.title) LIKE UPPER(CONCAT('%', :keyword, '%')) AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndTitleContainingIgnoreCaseAndActive(
            @Param("gameType") String gameType,
            @Param("keyword") String keyword,
            @Param("isActive") boolean isActive);
    
    /**
     * 게임 타입과 최소 평점으로 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.averageRating >= :minRating AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndMinimumRatingAndActive(
            @Param("gameType") String gameType,
            @Param("minRating") Float minRating,
            @Param("isActive") boolean isActive);
    
    /**
     * 강의 시간이 지정된 최대 시간 이하인 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.duration <= :maxDuration AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndMaxDurationAndActive(
            @Param("gameType") String gameType,
            @Param("maxDuration") Integer maxDuration,
            @Param("isActive") boolean isActive);
    
    /**
     * 가격 범위 내의 강의를 검색합니다.
     */
    @Query("SELECT l FROM Lecture l WHERE l.gameType = :gameType AND l.price BETWEEN :minPrice AND :maxPrice AND l.isActive = :isActive")
    List<Lecture> findByGameTypeAndPriceRangeAndActive(
            @Param("gameType") String gameType,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("isActive") boolean isActive);
}