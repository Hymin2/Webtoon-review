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
    public static class PlatformSelectResult {

        private Long webtoonId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayOfWeekSelectResult {

        private Long webtoonId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreSelectResult {

        private Long webtoonId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSelectResult {

        private Long webtoonId;
        private String name;
    }
}
