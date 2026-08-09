package com.hymin.webtoon_review.webtoon.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class WebtoonResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebtoonListResponse {

        private List<WebtoonListElement> webtoonListElements;
        private String next;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebtoonListElement {

        private Long id;
        private String name;
        private String thumbnail;
        private String authorName;
        private String dayOfWeek;
        private Integer starRating;
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
