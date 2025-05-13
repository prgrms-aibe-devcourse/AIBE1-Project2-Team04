package com.reboot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorProfileDTO {
    // Member 정보
    private String nickname;
    private String phone;

    // Instructor 정보 (추가/수정 가능한 부분)
    private String career;
    private String description;

    // Game 정보
    private String gameType;
    private String gameTier;
    private String gamePosition;
}