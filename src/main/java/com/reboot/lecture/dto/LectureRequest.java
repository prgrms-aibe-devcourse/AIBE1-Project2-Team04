package com.reboot.lecture.dto;

import com.reboot.lecture.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 강의 생성 및 수정 요청을 위한 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequest {
    private String title; // 강의 제목
    private String description; // 강의 상세 설명
    private String gameType; // 게임 타입(장르)
    private BigDecimal price; // 강의 가격
    private String imageUrl; // 강의 이미지 URL
    private Integer duration; // 강의 기간(일)
    private String lectureRank; // 랭크(티어)
    private String position; // 포지션


    // 강사 정보는 별도로 설정(서비스 계층에서 처리)

    public Lecture toEntity() {
        // 빌더 패턴을 사용하여 DTO 값으로 엔티티 생성
        return Lecture.builder()
                .title(this.title) // 제목
                .description(this.description) // 설명
                .gameType(this.gameType) // 게임 타입(장르)
                .price(this.price) // 가격
                .imageUrl(this.imageUrl) // 이미지 URL
                .duration(this.duration) // 기간
                .lectureRank(this.lectureRank) // 랭크(티어)
                .position(this.position) // 포지션
                // averageRating, totalMembers, reviewCount는 기본값(0)으로 생성
                // isActive는 기본값(true)으로 생성
                .build();
    }
}