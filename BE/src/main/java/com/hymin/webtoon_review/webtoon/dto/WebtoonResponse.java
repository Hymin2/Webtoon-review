package com.hymin.webtoon_review.webtoon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class WebtoonResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebtoonSimple {

        private Long id;
        private String name;
        private String thumbnail;
        private Integer recommendationCount;
        private Integer starScore;
        private Integer totalPopularityScore;
        private Integer manPopularityScore;
        private Integer femalePopularityScore;
        private String updatedAt;
        private String authorName;
        private String dayOfWeek;
        private String genre;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebtoonDetails {

        private Long id;
        private String name;
        private String thumbnail;
        private String description;
        private String platform;
        private Integer views;
        private Integer recommendationCount;
        private Integer starScore;
        private Integer manPopularityScore;
        private Integer femalePopularityScore;
        private Boolean isRecommended;
        private Boolean isBookmarked;
        private String authorName;
        private String dayOfWeek;
        private String genre;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {

        private Long id;
        private String name;
        private String updatedAt;
    }
}
