package com.reboot.auth.dto;

public record SignupDetailsDTO(
        String username,
        String career,
        String description,
        String profileImage,
        String gameType,
        String gameTier,
        String gamePosition
) {
}
