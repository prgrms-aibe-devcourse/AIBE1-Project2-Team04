package com.reboot.auth.repository;

import com.reboot.auth.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


// 강사 엔티티에 접근하기 위한 레포지토리 인터페이스
@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {


    // 사용자 ID로 강사 정보 조회
    Optional<Instructor> findByMemberId(Long MemberId);


    // 닉네임으로 강사 정보 조회
    Optional<Instructor> findByNickname(String nickname);


    // 활성화된 모든 강사 목록 조회
    List<Instructor> findByIsActiveTrue();


    // 특정 게임에 전문성을 가진 강사 목록 조회
    // LIKE 연산자를 사용하여 쉼표로 구분된 게임 목록에서 검색
    @Query(value = "SELECT DISTINCT * FROM Instructor i" +
            "WHERE i.is_active = true " +
            "AND (:gameType IS NULL OR REGEXP_LIKE(i.expertise_game_types, REPLACE(:gameType, ' ', '|')))",
            nativeQuery = true)
    List<Instructor> findByExpertiseGameType(@Param("gameType") String gameType);


    // 특정 포지션에 전문성을 가진 강사 목록 조회
    // LIKE 연산자를 사용하여 쉼표로 구분된 포지션 목록에서 검색
    @Query(value = "SELECT DISTINCT * FROM Instructor i " +
            "WHERE i.is_active = true " +
            "AND (:position IS NULL OR REGEXP_LIKE(i.expertise_positions, REPLACE(:position, ' ', '|')))",
            nativeQuery = true)
    List<Instructor> findByExpertisePosition(@Param("position") String position);


    // 인기도 순으로 정렬된 강사 목록 조회 (기본 정렬)
    // 인기도 계산: 별점 + (리뷰 수/100.0) + (총 수강생 수/100.0)
    @Query("SELECT i FROM Instructor i WHERE i.isActive = true " +
            "ORDER BY (i.averageRating + (i.reviewNum / 100.0) + (i.totalMembers / 100.0)) DESC")
    List<Instructor> findByIsActiveTrueOrderByPopularityDesc();



    // 최신 등록 순으로 정렬된 강사 목록 조회
    List<Instructor> findByIsActiveTrueOrderByCreatedAtDesc();


    // 리뷰 많은 순으로 정렬된 강사 목록 조회
    List<Instructor> findByIsActiveTrueOrderByReviewNumDesc();

    // 수강생 수 순으로 정렬된 강사 목록 조회
    List<Instructor> findByIsActiveTrueOrderByTotalMembersDesc();



    // 닉네임이나 소개에 특정 키워드가 포함된 강사 검색
    // 공백으로 구분된 복수 키워드 지원
    @Query(value = "SELECT DISTINCT * FROM Instructor i " +
            "WHERE i.is_active = true " +
            "AND (:keyword IS NULL OR " +
            "REGEXP_LIKE(LOWER(i.nickname), LOWER(REPLACE(:keyword, ' ', '|'))) " +
            "OR REGEXP_LIKE(LOWER(i.description), LOWER(REPLACE(:keyword, ' ', '|')) ))",
            nativeQuery = true)
    List<Instructor> searchInstructors(@Param("keyword") String keyword);


    // 삭제되지 않은 활성화된 강사 목록 조회
    @Query("SELECT i FROM Instructor i WHERE i.isActive = true AND i.deletedAt IS NULL")
    List<Instructor> findActiveInstructors();
}