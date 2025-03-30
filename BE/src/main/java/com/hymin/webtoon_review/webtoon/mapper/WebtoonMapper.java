package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import java.util.List;

public class WebtoonMapper {

    public static List<Category> toWebtoonCategoryList(List<Genre> genres) {
        return genres.stream()
            .map((g) -> Category.builder()
                .id(g.getId())
                .name(g.getName())
                .updatedAt(g.getUpdatedAt().toString())
                .build())
            .toList();
    }

    public static WebtoonPopularityScore toWebtoonPopularityScore(Long id, Integer score) {
        return WebtoonPopularityScore.builder()
            .id(id)
            .score(score)
            .build();
    }
}
