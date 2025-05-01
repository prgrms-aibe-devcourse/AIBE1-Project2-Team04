package com.reboot.survey.dto;

import com.reboot.survey.entity.enums.*;
import lombok.Data;

@Data
public class SurveyRequest {
    private GameType gameType;
    private String gameTier;
    private String gamePosition;
    private SkillLevel skillLevel;
    private LearningGoal learningGoal;
    private AvailableTime availableTime;
    private LecturePreference lecturePreference;
}