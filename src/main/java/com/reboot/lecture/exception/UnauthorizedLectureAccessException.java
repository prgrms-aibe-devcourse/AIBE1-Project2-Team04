package com.reboot.lecture.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


// 권한이 없는 강의 접근 시 발생하는 예외
// 강사가 자신의 강의가 아닌 다른 강의에 접근할 때 발생
@ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden
public class UnauthorizedLectureAccessException extends RuntimeException {

    // 예외 메시지
    public UnauthorizedLectureAccessException(String message) {
        super(message);
    }


    // 메시지와 원인
    public UnauthorizedLectureAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}