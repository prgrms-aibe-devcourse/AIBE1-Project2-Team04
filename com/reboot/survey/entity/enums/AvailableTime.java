package com.reboot.survey.entity.enums;

public enum AvailableTime {
    VERY_SHORT("매우 짧음", "하루 30분 미만"),
    SHORT("짧음", "하루 1시간 미만"),
    MEDIUM("보통", "하루 1-2시간"),
    LONG("긴 편", "하루 2-4시간"),
    VERY_LONG("매우 긴 편", "하루 4시간 이상");
    
    private String displayName;
    private String description;
    
    AvailableTime(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}