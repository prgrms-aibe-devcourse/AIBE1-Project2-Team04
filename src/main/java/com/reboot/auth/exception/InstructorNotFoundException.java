package com.reboot.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


// 강사 정보를 찾을 수 없을 때 발생하는 예외
@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Not Found
public class InstructorNotFoundException extends RuntimeException {


    // 기본 메시지
    public InstructorNotFoundException(String message) {
        super(message);
    }


    // 메시지와 원인
    public InstructorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}