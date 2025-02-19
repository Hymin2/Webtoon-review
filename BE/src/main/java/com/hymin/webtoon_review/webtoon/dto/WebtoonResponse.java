package com.hymin.webtoon_review.webtoon.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class WebtoonResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebtoonSimple {

        private Long id;
        private String name;
        private String thumbnail;
        private String updatedAt;
        private Integer recommendationCount;
        private Integer starScore;
        private Integer totalPopularityScore;
        private Integer manPopularityScore;
        private Integer femalePopularityScore;
        @Setter
        private String authorName;
        @Setter
        private String dayOfWeek;
        @Setter
        private String genre;

        public WebtoonSimple(Long id, String name, String thumbnail, Integer recommendationCount,
            Integer starScore, Integer totalPopularityScore, Integer manPopularityScore,
            Integer femalePopularityScore, LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.thumbnail = thumbnail;
            this.starScore = starScore;
            this.totalPopularityScore = totalPopularityScore == null ? 0 : totalPopularityScore;
            this.recommendationCount = recommendationCount == null ? 0 : recommendationCount;
            this.manPopularityScore = manPopularityScore == null ? 0 : manPopularityScore;
            this.femalePopularityScore = femalePopularityScore == null ? 0 : femalePopularityScore;
            this.updatedAt = updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        public void addDomain(String domain) {
            this.thumbnail = domain + thumbnail;
        }
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
        @Setter
        private String authorName;
        @Setter
        private String dayOfWeek;
        @Setter
        private String genre;

        public WebtoonDetails(Long id, String name, String thumbnail, String description,
            String platform, Integer views, Integer starScore, Integer manPopularityScore,
            Integer femalePopularityScore,
            Integer recommendationCount, Integer isRecommended,
            Integer isBookmarked) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.thumbnail = thumbnail;
            this.platform = platform;
            this.views = views;
            this.starScore = starScore;
            this.manPopularityScore = manPopularityScore == null ? 0 : manPopularityScore;
            this.femalePopularityScore = femalePopularityScore == null ? 0 : femalePopularityScore;
            this.recommendationCount = recommendationCount == null ? 0 : recommendationCount;
            this.isRecommended = isRecommended != null;
            this.isBookmarked = isBookmarked != null;
        }

        public void addDomain(String domain) {
            this.thumbnail = domain + thumbnail;
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
