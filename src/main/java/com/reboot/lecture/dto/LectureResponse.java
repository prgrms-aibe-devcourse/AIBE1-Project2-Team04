package com.reboot.lecture.dto;

import com.reboot.lecture.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


// 강의 정보 응답을 위한 DTO
// 상세 설명과 같은 크고 자세한 데이터는 제외
// 목록 표시에 필요한 정보만 포함

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureResponse {
    private String id; // 강의 고유 식별자
    private String Nickname; // 강사 닉네임 (ID가 아닌 닉네임 표시)
    private String title; // 강의 제목
    private String gameType; // 게임 타입(장르)
    private BigDecimal price; // 강의 가격
    private String imageUrl; // 강의 이미지 URL
    private String lectureRank; // 랭크(티어)
    private String position; // 포지션
    private Float averageRating; // 평균 별점
    private Integer reviewCount; // 총 리뷰 개수
    private Integer totalMembers; // 총 수강생 수
    private LocalDateTime createdAt; // 강의 등록 시간 (최신순 정렬에 필요)


    public static LectureResponse fromEntity(Lecture lecture) {
        return LectureResponse.builder()
                .id(lecture.getId()) // 강의 ID
                .Nickname(lecture.getInstructor().getNickname()) // 강사 닉네임 (ID 아님)
                .title(lecture.getTitle()) // 강의 제목
                .gameType(lecture.getGameType()) // 게임 타입
                .price(lecture.getPrice()) // 가격
                .imageUrl(lecture.getImageUrl()) // 이미지 URL
                .lectureRank(lecture.getLectureRank()) // 랭크
                .position(lecture.getPosition()) // 포지션
                .averageRating(lecture.getAverageRating()) // 평균 별점
                .reviewCount(lecture.getReviewCount()) // 총 리뷰 개수
                .totalMembers(lecture.getTotalMembers()) // 총 수강생 수
                .createdAt(lecture.getCreatedAt()) // 생성 시간
                .build();
    }


    public static List<LectureResponse> fromEntities(List<Lecture> lectures) {
        return lectures.stream()
                .map(LectureResponse::fromEntity)
                .collect(Collectors.toList());
    }
}