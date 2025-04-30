package com.reboot.auth.dto;

public record SignupDTO(
        String username,
        String password,
        String name,
        String email,
        String nickname
) {
}