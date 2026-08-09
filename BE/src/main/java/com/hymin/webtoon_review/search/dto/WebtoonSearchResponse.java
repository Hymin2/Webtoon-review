package com.hymin.webtoon_review.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonSearchResponse {

    private Long id;
    private String name;
    private String thumbnail;
    private String authors;
}
