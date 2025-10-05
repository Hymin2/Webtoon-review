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

        public WebtoonDetails(
            Long id,
            String name,
            String thumbnail,
            String description,
            String platform,
            Integer view,
            Integer recommendationCount,
            Integer starScore,
            Integer manPopularityScore,
            Integer femalePopularityScore,
            Integer isRecommended,
            Integer isBookmarked,
            String authorName,
            String dayOfWeek,
            String genre
        ) {
            this.id = id;
            this.name = name;
            this.thumbnail = thumbnail;
            this.description = description;
            this.platform = platform;
            this.views = view;
            this.recommendationCount = recommendationCount;
            this.starScore = starScore;
            this.manPopularityScore = manPopularityScore;
            this.femalePopularityScore = femalePopularityScore;
            this.isRecommended = isRecommended != null;
            this.isBookmarked = isBookmarked != null;
            this.authorName = authorName;
            this.dayOfWeek = dayOfWeek;
            this.genre = genre;
        }
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
