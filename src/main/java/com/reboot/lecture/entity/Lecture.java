package com.reboot.lecture.entity;

import com.reboot.auth.entity.Instructor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@ToString(exclude = "instructor")
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // lecture_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor; // FK: instructor_id

    @Column(nullable = false)
    private String title; // 강의 제목 -> 홈 화면 강의 목록에 표시

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // 강의 상세 설명(상세 페이지 표시)

    @Column(name = "game_type", nullable = false)
    private String gameType; // 게임 장르

    @Column(nullable = false)
    private BigDecimal price; // 강의 가격

    @Column(name = "image_url")
    private String imageUrl; // 강의 이미지 URL -> 홈 화면 강의 목록에 표시

    @Column(nullable = false)
    private Integer duration; // 강의 기간(일) -> 수강신청 시 필요

    @Column(nullable = false)
    private String rank; // 티어 필터 -> 홈 - 강의 목록 화면에서 필요

    @Column(nullable = false)
    private String position; // 포지션 필터 -> 홈 - 강의 목록 화면에서 필요

    @Column(nullable = false)
    private Float averageRating = 0.0f; // 평균 별점 - 홈 화면(강의 목록) 표시

    @Column(nullable = false)
    private Integer totalMembers = 0; // 총 수강생 수 - 홈 화면(강의 목록) 표시

    @Column(nullable = false)
    private Integer reviewCount = 0; // 리뷰 개수 - 홈 화면(강의 목록) 표시

    @Column(nullable = false)
    private LocalDateTime createdAt; // 강의 등록 시간 -> 신규 강의 표시 및 최신순 정렬에 사용

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 강의 수정 시간 -> 컨텐츠 업데이트 추적에 사용한다는데..

    @Column
    private LocalDateTime deletedAt; // 강의 삭제 시간

    @Column(nullable = false)
    private boolean isActive; // 강의 활성화 상태 -> 구매 시 활성화 / 기간 만료시 비활성화

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // 엔티티 생성 시 현재 시간 자동 설정
        updatedAt = LocalDateTime.now(); // 엔티티 생성 시 현재 시간 자동 설정
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // 엔티티 수정 시 현재 시간 자동 설정
    }
}
