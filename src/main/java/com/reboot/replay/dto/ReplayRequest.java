package com.reboot.replay.dto;

import lombok.*;

// 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplayRequest {
    private Long reservationId;
    private String fileUrl;
}