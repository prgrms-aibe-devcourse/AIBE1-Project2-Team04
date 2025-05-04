package com.reboot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class profileDTO {
    private String name;
    private String nickname;
    private String email;
    private String phone;
}
