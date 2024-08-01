package com.hymin.webtoon_review.webtoon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class WebtoonSelectResult {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Platform {

        private Long webtoonId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayOfWeek {

        private Long webtoonId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Genre {

        private Long webtoonId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {

        private Long webtoonId;
        private String name;
    }
}
