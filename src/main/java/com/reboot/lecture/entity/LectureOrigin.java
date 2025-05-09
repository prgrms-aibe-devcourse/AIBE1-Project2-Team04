//package com.reboot.lecture.entity;
//
//import com.reboot.auth.entity.Instructor;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//
//// 강의 정보 저장
//// 사용자에게 제공되는 강의의 모든 기본 정보와 통계 데이터
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Lecture {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "lecture_id")
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "instructor_id", nullable = false)
//    private Instructor instructor; // 강사 정보 (다대일 관계)
//
//    @Column(nullable = false)
//    private String title; // 강의 제목 -> 홈 화면 강의 목록에 표시
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String description; // 강의 상세 설명(상세 페이지 표시)
//
//    @Column(name = "game_type", nullable = false)
//    private String gameType; // 게임 장르
//
//    @Column(nullable = false)
//    private BigDecimal price; // 강의 가격
//
//    @Column(name = "image_url")
//    private String imageUrl; // 강의 이미지 URL -> 홈 화면 강의 목록에 표시
//
//    @Column(nullable = false)
//    private Integer duration; // 강의 기간(일) -> 수강신청 시 필요
//
//    @Column(name = "lecture_rank", nullable = false)
//    private String lectureRank; // 랭크(티어) 필터 -> 홈 - 강의 목록 화면에서 필요
//    // 'rank'는 MySQL 예약어이므로 'lecture_rank'로 컬럼명 지정
//
//    @Column(nullable = false)
//    private String position; // 포지션 필터 -> 홈 - 강의 목록 화면에서 필요
//
//    @Column(nullable = false)
//    private Float averageRating; // 평균 별점 - 홈 화면(강의 목록) 표시
//
//    @Column(nullable = false)
//    private Integer totalMembers; // 총 수강생 수 - 홈 화면(강의 목록) 표시
//
//    @Column(nullable = false)
//    private Integer reviewCount; // 총 리뷰 개수 - 홈 화면(강의 목록) 표시
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt; // 강의 등록 시간 -> 신규 강의 표시 및 최신순 정렬에 사용
//
//    @Column(nullable = false)
//    private LocalDateTime updatedAt; // 강의 수정 시간 -> 컨텐츠 업데이트 추적에 사용
//
//    @Column
//    private LocalDateTime deletedAt; // 강의 삭제 시간 (소프트 삭제 구현 시 사용)
//
//    @Column(nullable = false)
//    private Boolean isActive; // 강의 활성화 상태 -> 홈에서 표시 여부
//    // 권한 여부와는 다름 -> 수강생별 강의 접근 권한은 별도로 관리!
//
//
//    // 기본값 및 초기화
//    public static class LectureBuilder {
//        private Float averageRating = 0.0f;
//        private Integer totalMembers = 0;
//        private Integer reviewCount = 0;
//        private Boolean isActive = true;
//    }
//
//
//    //엔티티 생성 시 호출, 생성 시간과 수정 시간을 현재로 설정
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//    }
//
//
//    //엔티티 수정 시 호출, 수정 시간을 현재로 업데이트
//    @PreUpdate
//    protected void onUpdate() {
//        updatedAt = LocalDateTime.now();
//    }
//
//
//    // 커스텀 toString 메서드
//    // 순환 참조 방지, instructor의 ID만 포함
//    @Override
//    public String toString() {
//        return "Lecture{" +
//                "id='" + id + '\'' +
//                ", instructorId='" + (instructor != null ? instructor.getInstructorId() : null) + '\'' +
//                ", title='" + title + '\'' +
//                ", gameType='" + gameType + '\'' +
//                ", price=" + price +
//                ", lectureRank='" + lectureRank + '\'' +
//                ", position='" + position + '\'' +
//                ", averageRating=" + averageRating +
//                ", totalMembers=" + totalMembers +
//                ", reviewCount=" + reviewCount +
//                ", isActive=" + isActive +
//                '}';
//    }
//}