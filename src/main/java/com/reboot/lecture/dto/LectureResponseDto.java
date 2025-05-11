package com.reboot.lecture.dto;

import com.reboot.lecture.entity.Lecture;
import com.reboot.lecture.entity.LectureInfo;
import com.reboot.lecture.entity.LectureMetaData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureResponseDto {
    private String id; // 강의 고유 식별자
    private String Nickname; // 강사 닉네임 (ID가 아닌 닉네임 표시)
    private String title; // 강의 제목
    private String gameType; // 게임 타입(장르)
    private Integer price; // 강의 가격
    private String imageUrl; // 강의 이미지 URL
    private String lectureRank; // 랭크(티어)
    private String position; // 포지션
    private Float averageRating; // 평균 별점
    private Integer reviewCount; // 총 리뷰 개수
    private Integer totalMembers; // 총 수강생 수
    private LocalDateTime createdAt; // 강의 등록 시간 (최신순 정렬에 필요)

    // 추가된 필드들
    private String videoUrl; // 강의 동영상 URL
    private String previewUrl; // 미리보기 동영상 URL
    private String materialUrls; // 강의 자료 URL들

    public static LectureResponseDto fromEntity(Lecture lecture) {
        LectureInfo info = lecture.getInfo();
        LectureMetaData metadata = lecture.getMetadata();
        if (info == null || metadata == null) {
            throw new IllegalArgumentException("Lecture info or metadata is not loaded: " + lecture.getId());
        }

        return LectureResponseDto.builder()
                .id(String.format("LECTURE-%d", lecture.getId())) // 강의 ID + 프리픽스 추가
                // ex) LECTURE-1001, 1002, ...
                .Nickname(lecture.getInstructor().getNickname()) // 강사 닉네임 (ID 아님)

                // LectureInfo 에서 가져오는 필드들
                .title(info.getTitle()) // 강의 제목
                .gameType(info.getGameType()) // 게임 타입
                .price(info.getPrice()) // 가격
                .imageUrl(info.getImageUrl()) // 이미지 URL
                .lectureRank(info.getLectureRank()) // 랭크
                .position(info.getPosition()) // 포지션
                .videoUrl(info.getVideoUrl()) // 강의 동영상 URL
                .previewUrl(info.getPreviewUrl()) // 미리보기 동영상 URL
                .materialUrls(info.getMaterialUrls()) // 강의 자료 URL들

                // LectureMetaData 에서 가져오는 필드들
                .averageRating(metadata.getAverageRating()) // 평균 별점
                .reviewCount(metadata.getReviewCount()) // 총 리뷰 개수
                .totalMembers(metadata.getTotalMembers()) // 총 수강생 수
                .createdAt(metadata.getCreatedAt()) // 생성 시간
                .build();
    }

    public static List<LectureResponseDto> fromEntities(List<Lecture> lectures) {
        return lectures.stream()
                .map(LectureResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}