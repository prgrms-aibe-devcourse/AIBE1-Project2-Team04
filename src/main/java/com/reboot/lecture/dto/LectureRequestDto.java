package com.reboot.lecture.dto;

import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 강의 생성 및 수정 요청을 위한 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequestDto {
    private String title; // 강의 제목
    private String description; // 강의 상세 설명
    private String gameType; // 게임 타입(장르)
    private Integer price; // 강의 가격
    private String imageUrl; // 강의 이미지 URL
    private Integer duration; // 강의 기간(일)
    private String lectureRank; // 랭크(티어)
    private String position; // 포지션


    // 강사 정보는 별도로 설정(서비스 계층에서 처리)

    public Lecture toEntity() {
        // 1. LectureInfo 생성 및 연결
        LectureInfo info = LectureInfo.builder()
                .title(this.title) // 제목
                .description(this.description) // 설명
                .gameType(this.gameType) // 게임 타입(장르)
                .price(this.price) // 가격
                .imageUrl(this.imageUrl) // 이미지 URL
                .duration(this.duration) // 기간
                .lectureRank(this.lectureRank) // 랭크(티어)
                .position(this.position) // 포지션
                .build();

        // 기본 Lecture 생성 - 내부 Builder 클래스에서 metadata는 자동 생성됨
        Lecture lecture = Lecture.builder()
                .info(info)
                .build();

        return lecture;
    }


    // 기존 엔티티를 업데이트합니다.
    public void updateEntity(Lecture lecture) {
        if (lecture == null || lecture.getInfo() == null) {
            throw new IllegalArgumentException("Invalid lecture entity or info is null");
        }

        LectureInfo info = lecture.getInfo();

        // LectureInfo 업데이트
        info.setTitle(this.title);
        info.setDescription(this.description);
        info.setGameType(this.gameType);
        info.setPrice(this.price);
        info.setImageUrl(this.imageUrl);
        info.setDuration(this.duration);
        info.setLectureRank(this.lectureRank);
        info.setPosition(this.position);

        // 메타데이터의 업데이트 일시는 @PreUpdate에서 자동 갱신됨
    }
}