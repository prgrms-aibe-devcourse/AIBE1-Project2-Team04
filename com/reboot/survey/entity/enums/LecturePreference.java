package com.reboot.survey.entity.enums;

public enum LecturePreference {
    ONE_ON_ONE("1:1 개인 강의"),
    OFFLINE("오프라인 강의"),
    FILE_PROVIDED("강의 자료 제공"),
    REPLAY_ANALYSIS("게임 리플레이 분석"),
    GROUP("그룹 강의");
    
    private String displayName;
    
    LecturePreference(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}