package com.reboot.survey.entity;

import com.reboot.auth.entity.Member;
import com.reboot.survey.entity.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 정보 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    private String gameTier;
    private String gamePosition;

    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @Enumerated(EnumType.STRING)
    private LearningGoal learningGoal;

    @Enumerated(EnumType.STRING)
    private AvailableTime availableTime;

    @Enumerated(EnumType.STRING)
    private LecturePreference lecturePreference;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = java.time.LocalDateTime.now();
    }
}