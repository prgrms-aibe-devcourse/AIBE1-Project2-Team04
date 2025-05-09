package com.reboot.survey.entity.enums;

public enum SkillLevel {
    BEGINNER("초보"),
    INTERMEDIATE("평범"),
    ADVANCED("고수");

    private final String label;

    SkillLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

