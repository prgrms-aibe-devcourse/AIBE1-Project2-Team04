package com.reboot.lecture.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


// 강의를 찾을 수 없을 때, 예외 처리
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LectureNotFoundException extends RuntimeException {


    // 단순 오류 메시지만 필요한 경우
    // ex) "강의를 찾을 수 없습니다 [ID]"
    public LectureNotFoundException(String message) {
        super(message);
    }


    // 메시지와 원인 포함
    // 다른 예외로 인해 이 예외가 발생한 경우
    // ex) DB 오류로 인해 강의를 찾을 수 없는 경우 등
    public LectureNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}