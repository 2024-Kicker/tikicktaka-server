package com.example.tikicktaka.domain.enums;

public enum LimitTime {
    MINUTES_30(30),
    HOURS_1(60),
    HOURS_2(120),
    HOURS_3(180);

    private final int minutes;

    LimitTime(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }
}
