package com.reboot.survey.entity.enums;

public enum AvailableTime {
    ONE_HOUR_PLUS("주 1시간 이상"),
    FIVE_HOUR_PLUS("주 5시간 이상"),
    WEEKDAY("평일 내내"),
    WEEKEND("주말 내내"),
    WHOLE_WEEK("일주일 내내");

    private final String label;

    AvailableTime(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

