package com.hymin.webtoon_review.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TableDataRange {
    DAY_OF_WEEK(1, 7),
    PLATFORM(1, 8),
    GENRE(1, 18);

    private Integer minId;
    private Integer maxId;
}
