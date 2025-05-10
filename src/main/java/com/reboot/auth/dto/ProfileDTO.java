package com.reboot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDTO {

    // 읽기 전용, 변경 불가
    private String name;
    private String username;
    private String email;

    // 변경 가능
    private String nickname; // 2-50자 사이
    private String phone; // 형식: 010-1234-5678, 선택 입력
}