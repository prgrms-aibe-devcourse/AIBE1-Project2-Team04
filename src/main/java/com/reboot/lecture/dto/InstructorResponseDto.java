package com.reboot.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorResponseDto {
    private Long instructorId;
    private String nickname;
    private String career;
    private String description;
    private int reviewNum;
    private double averageRating;
    private Integer totalMembers;
    private LocalDateTime createdAt;

    // Game 정보
    private String gameType;
    private String gamePosition;
    private String gameTier;
}