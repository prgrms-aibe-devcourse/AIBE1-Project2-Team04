package com.reboot.reservation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplayResponseDto {
    private Long replayId;
    private String fileUrl;
    private LocalDateTime date;
    // 필요한 추가 필드...
}