package com.reboot.survey.entity;

import com.reboot.auth.entity.Member;
import com.reboot.survey.entity.enums.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

@Entity
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
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
}
