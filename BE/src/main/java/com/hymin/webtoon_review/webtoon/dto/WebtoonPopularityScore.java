package com.hymin.webtoon_review.webtoon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonPopularityScore {

    private Long id;
    private Integer score;
}
