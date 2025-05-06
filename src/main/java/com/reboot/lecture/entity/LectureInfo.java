package com.reboot.lecture.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class LectureInfo {
    private String title;
    private String description;
    private String gameType;
    private BigDecimal price;
    private String imageUrl;
    private Integer duration;
    private String rank_;
    private String position;
}

