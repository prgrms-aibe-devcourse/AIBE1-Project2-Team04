package com.reboot.lecture.dto;

import com.reboot.lecture.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 강의 정보 전송을 위한 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureDto {
    private Long id; // 강의 고유 식별자
    private Long instructorId; // 강사 고유 식별자
    private String instructorName; // 강사 이름
    private String title; // 강의 제목
    private String description; // 강의 상세 설명
    private String gameType; // 게임 타입(장르)
    private Integer price; // 강의 가격
    private String imageUrl; // 강의 이미지 URL
    private Integer duration; // 강의 기간(일)
    private String lectureRank; // 랭크(티어) 필터
    private String position; // 포지션 필터
    private Float averageRating; // 평균 별점
    private Integer totalMembers; // 총 수강생 수
    private Integer reviewCount; // 총 리뷰 개수
    private LocalDateTime createdAt; // 강의 등록 시간


    public static LectureDto fromEntity(Lecture lecture) {
        return LectureDto.builder()
                .id(lecture.getId()) // 강의 ID
                .instructorId(lecture.getInstructor().getInstructorId()) // 강사 ID
                .instructorName(lecture.getInstructor().getNickname()) // 강사 이름

                // getInfo
                .title(lecture.getInfo().getTitle()) // 강의 제목
                .description(lecture.getInfo().getDescription()) // 강의 설명
                .gameType(lecture.getInfo().getGameType()) // 게임 타입(장르)
                .price(lecture.getInfo().getPrice()) // 가격
                .imageUrl(lecture.getInfo().getImageUrl()) // 이미지 URL
                .duration(lecture.getInfo().getDuration()) // 기간
                .lectureRank(lecture.getInfo().getLectureRank()) // 랭크
                .position(lecture.getInfo().getPosition()) // 포지션

                // getMetadata
                .averageRating(lecture.getMetadata().getAverageRating()) // 평균 별점
                .totalMembers(lecture.getMetadata().getTotalMembers()) // 총 수강생 수
                .reviewCount(lecture.getMetadata().getReviewCount()) // 총 리뷰 개수
                .createdAt(lecture.getMetadata().getCreatedAt()) // 생성 시간
                .build();
    }
}