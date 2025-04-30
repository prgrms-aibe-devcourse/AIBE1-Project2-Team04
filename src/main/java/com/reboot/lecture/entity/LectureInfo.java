package com.reboot.lecture.entity;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class LectureInfo {
    private String title;
    private String description;
    private String gameType;
    private BigDecimal price;
    private String imageUrl;
    private Integer duration;
    private String rank;
    private String position;
}

