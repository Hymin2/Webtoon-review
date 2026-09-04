package com.hymin.webtoon_review.webtoon.entity.enums;

public enum Status {
    연재중, 휴재중, 완결;

    public String toString() {
        return this.name();
    }
}
