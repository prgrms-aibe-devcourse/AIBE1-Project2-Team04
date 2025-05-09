package com.reboot.survey.entity.enums;

public enum GameType {
    LOL("리그 오브 레전드"),
    OVERWATCH("오버워치"),
    PUBG("배틀그라운드"),
    FIFA("피파 온라인"),
    VALORANT("발로란트"),
    STARCRAFT("스타크래프트");
    
    private String displayName;
    
    GameType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}