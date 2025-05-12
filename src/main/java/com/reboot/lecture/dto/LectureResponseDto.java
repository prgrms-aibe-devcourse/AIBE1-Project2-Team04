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


// 강의 정보 응답을 위한 DTO
// 상세 설명과 같은 크고 자세한 데이터는 제외
// 목록 표시에 필요한 정보만 포함

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureResponseDto {
    private String id; // 강의 고유 식별자
    private String instructorNickname; // 강사 닉네임 (ID가 아닌 닉네임 표시)
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

    // 추가 필드
    private String description; // 강의 설명
    private Integer duration; // 강의 시간(분)
    private Long rawId; // 원본 ID (프리픽스 없는)

    public static LectureResponseDto fromEntity(Lecture lecture) {
        LectureInfo info = lecture.getInfo();
        LectureMetaData metadata = lecture.getMetadata();
        if (info == null || metadata == null) {
            throw new IllegalArgumentException("Lecture info or metadata is not loaded: " + lecture.getId());
        }

        return LectureResponseDto.builder()
                .id(String.format("LECTURE-%d", lecture.getId()))
                .rawId(lecture.getId()) // 원본 ID 추가
                .instructorNickname(lecture.getInstructor().getNickname())
                .title(info.getTitle())
                .gameType(info.getGameType())
                .price(info.getPrice())
                .imageUrl(info.getImageUrl())
                .lectureRank(info.getLectureRank())
                .position(info.getPosition())
                .averageRating(metadata.getAverageRating())
                .reviewCount(metadata.getReviewCount())
                .totalMembers(metadata.getTotalMembers())
                .createdAt(metadata.getCreatedAt())

                // 새로 추가된 필드 매핑
                .description(truncateDescription(info.getDescription()))
                .duration(info.getDuration())
                .build();
    }

    // 설명을 위한 헬퍼 메소드 - 3문장으로 제한
    private static String truncateDescription(String description) {
        if (description == null) {
            return "강의 상세 페이지에서 자세한 내용을 확인하세요.";
        }

        // 문장 구분 패턴
        String[] sentences = description.split("[.!?]\\s*");

        if (sentences.length <= 3) {
            return description;
        }

        // 3개 문장만 이어붙이기
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(3, sentences.length); i++) {
            result.append(sentences[i]);
            // 마침표 추가
            result.append(". ");
        }

        return result.toString().trim();
    }

    // 기존 fromEntities 메소드 유지
    public static List<LectureResponseDto> fromEntities(List<Lecture> lectures) {
        return lectures.stream()
                .map(LectureResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}