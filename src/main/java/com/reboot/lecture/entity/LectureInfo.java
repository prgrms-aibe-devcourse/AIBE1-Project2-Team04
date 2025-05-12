package com.reboot.lecture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC) // - Embeddable 클래스라서 기본 생성자 필수
@AllArgsConstructor
@Builder
public class LectureInfo {
    @Column(nullable = false)
    private String title; // 강의 제목 -> 홈 화면 강의 목록에 표시

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // 강의 상세 설명(상세 페이지 표시)

    @Column(name = "game_type", nullable = false)
    private String gameType; // 게임 장르

    @Column(nullable = false)
    private Integer price; // 강의 가격

    @Column(name = "image_url")
    private String imageUrl; // 강의 이미지 URL -> 홈 화면 강의 목록에 표시

    @Column(nullable = false)
    private Integer duration; // 강의 기간(일) -> 수강신청 시 필요

    @Column(name = "lecture_rank", nullable = false)
    private String lectureRank; // 랭크(티어) 필터 -> 홈 - 강의 목록 화면에서 필요
    // 'rank'는 MySQL 예약어이므로 'lecture_rank'로 컬럼명 지정

    @Column(nullable = false)
    private String position; // 포지션 필터 -> 홈 - 강의 목록 화면에서 필요
}