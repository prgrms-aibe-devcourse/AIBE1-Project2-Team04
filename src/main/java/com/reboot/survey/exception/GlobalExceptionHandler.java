package com.reboot.survey.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException e, Model model) {
        log.error("엔티티를 찾을 수 없음: {}", e.getMessage());
        model.addAttribute("errorMessage", "요청하신 정보를 찾을 수 없습니다.");
        model.addAttribute("errorDetails", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception e, Model model) {
        log.error("일반 예외 발생: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "서비스 처리 중 오류가 발생했습니다.");
        model.addAttribute("errorDetails", e.getMessage());
        return "error/500";
    }
}