package com.reboot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupResponse {
    private boolean success;
    private String field;
    private String message;
}
