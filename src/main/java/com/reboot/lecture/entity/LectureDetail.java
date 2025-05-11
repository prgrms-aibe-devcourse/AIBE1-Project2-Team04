package com.reboot.lecture.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LectureDetail {
    @Id
    private Long id;  // Lecture ID와 매핑

    @OneToOne
    @MapsId
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(columnDefinition = "TEXT")
    private String overview;  // 강의 개요

    @Column(columnDefinition = "TEXT")
    private String learningObjectives;  // 학습 목표

    @Column(columnDefinition = "TEXT")
    private String prerequisites;  // 수강 전 필요 지식

    @Column(columnDefinition = "TEXT")
    private String targetAudience;  // 대상 수강생

    @Column(columnDefinition = "TEXT")
    private String curriculum;  // 커리큘럼

    @Column(columnDefinition = "TEXT")
    private String instructorBio;  // 강사 소개
}