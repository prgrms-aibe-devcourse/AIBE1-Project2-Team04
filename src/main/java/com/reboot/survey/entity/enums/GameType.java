package com.reboot.survey.entity.enums;

public enum GameType {
    LOL("리그오브레전드"),
    VALORANT("발로란트"),
    TFT("전략적팀전투");

    private final String displayName;

    GameType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
