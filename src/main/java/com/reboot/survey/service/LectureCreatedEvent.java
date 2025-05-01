package com.reboot.survey.service;

import com.reboot.lecture.entity.Lecture;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LectureCreatedEvent extends ApplicationEvent {
    private final Lecture lecture;

    public LectureCreatedEvent(Object source, Lecture lecture) {
        super(source);
        this.lecture = lecture;
    }
}