package com.reboot.lecture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureMetaData {
    @Builder.Default
    @Column(nullable = false)
    private Float averageRating = 0.0f; // 평균 별점 - 홈 화면(강의 목록) 표시

    @Builder.Default
    @Column(nullable = false)
    private Integer totalMembers = 0; // 총 수강생 수 - 홈 화면(강의 목록) 표시

    @Builder.Default
    @Column(nullable = false)
    private Integer reviewCount = 0; // 총 리뷰 개수 - 홈 화면(강의 목록) 표시

    @Column(nullable = false)
    private LocalDateTime createdAt; // 강의 등록 시간 -> 신규 강의 표시 및 최신순 정렬에 사용

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 강의 수정 시간 -> 컨텐츠 업데이트 추적에 사용

    // 메타데이터 빌더에 기본값 설정
    public static class LectureMetaDataBuilder {
        private Float averageRating = 0.0f;
        private Integer totalMembers = 0;
        private Integer reviewCount = 0;
    }
}