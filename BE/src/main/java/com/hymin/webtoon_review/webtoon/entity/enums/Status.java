package com.hymin.webtoon_review.webtoon.entity.enums;

public enum Status {
    ONGOING, PAUSED, COMPLETED;

    public String toString() {
        return this.name();
    }
}
