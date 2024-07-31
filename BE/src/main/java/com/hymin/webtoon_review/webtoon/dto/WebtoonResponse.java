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
    public static class WebtoonInfo {

        private Long id;
        private String name;
        private String description;
        private String thumbnail;
        private String platform;
        private List<String> authorName;
        private List<String> dayOfWeeks;
        private List<String> genres;
    }
}
