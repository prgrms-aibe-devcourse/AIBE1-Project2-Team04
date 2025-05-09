package com.reboot.survey.entity.enums;

public enum SkillLevel {
    BEGINNER("초보자"),
    INTERMEDIATE("중급자"),
    ADVANCED("고급자"),
    EXPERT("전문가");
    
    private String displayName;
    
    SkillLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}