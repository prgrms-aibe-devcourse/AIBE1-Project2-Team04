package com.reboot.survey.entity.enums;

public enum LearningGoal {
    ESCAPE_BEGINNER("초보 탈출"),
    IMPROVE_SKILL("실력 향상"),
    CLIMB_RANK("랭크 상승"),
    BECOME_PRO("프로 도전"),
    PLAY_TOGETHER("함께 플레이"),
    HOBBY("취미 활동"),
    EXPERT_ACTIVITY("전문가 활동");
    
    private String displayName;
    
    LearningGoal(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}