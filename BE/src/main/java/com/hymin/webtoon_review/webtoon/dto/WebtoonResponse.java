package com.hymin.webtoon_review.webtoon.dto;

import java.time.LocalDateTime;
import java.util.List;
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
    public static class WebtoonInfo {

        private Long id;
        private String name;
        private String description;
        private String thumbnail;
        private String platform;
        private LocalDateTime updatedAt;
        private Integer views;
        private Integer recommendedCount;
        private Boolean isRecommended;
        private Boolean isBookmarked;
        @Setter
        private List<String> authorName;
        @Setter
        private List<String> dayOfWeeks;
        @Setter
        private List<String> genres;

        public WebtoonInfo(Long id, String name, String description, String thumbnail,
            String platform, LocalDateTime updatedAt, Integer views, Integer recommendedCount,
            Integer isRecommended,
            Integer isBookmarked) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.thumbnail = thumbnail;
            this.platform = platform;
            this.updatedAt = updatedAt;
            this.views = views == null ? 0 : views;
            this.recommendedCount = recommendedCount == null ? 0 : recommendedCount;
            this.isRecommended = isRecommended != null;
            this.isBookmarked = isBookmarked != null;
        }
    }
}
