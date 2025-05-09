package com.reboot.lecture.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
public class LectureInfo {
    private String title;
    private String description;
    private String gameType;
    private Integer price;
    private String imageUrl;
    private Integer duration;
    private String rank_;
    private String position;
}