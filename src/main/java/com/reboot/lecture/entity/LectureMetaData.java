package com.reboot.lecture.entity;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class LectureMetaData {
    private Float averageRating;
    private Integer totalMembers;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private boolean isActive;
}