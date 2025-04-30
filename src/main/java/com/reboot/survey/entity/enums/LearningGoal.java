package com.reboot.survey.entity.enums;

public enum LearningGoal {
    ESCAPE_BEGINNER("초보 탈출"),
    IMPROVE_SKILL("실력 상승"),
    CLIMB_RANK("랭크 상승"),
    BECOME_PRO("프로게이머 데뷔"),
    PLAY_TOGETHER("함께 게임 하기"),
    HOBBY("취미 활동"),
    EXPERT_ACTIVITY("Gigs 전문가 활동");

    private final String label;

    LearningGoal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

