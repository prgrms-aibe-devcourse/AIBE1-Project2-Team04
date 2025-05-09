package com.reboot.survey.entity.enums;

public enum LecturePreference {
    ONE_ON_ONE("1:1 피드백"),
    OFFLINE("오프라인 강의"),
    FILE_PROVIDED("파일 제공"),
    REPLAY_ANALYSIS("리플레이 분석"),
    GROUP("그룹 강의");

    private final String label;

    private LecturePreference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

