package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.webtoon.dto.WebtoonListResultDto;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonListElement;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonListResponse;
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

    public static WebtoonListResponse toWebtoonListResponse(
            List<WebtoonListResultDto> webtoonList,
            String order
    ) {
        return WebtoonListResponse.builder()
                .webtoonListElements(
                        webtoonList.stream()
                                .map(w -> WebtoonListElement.builder()
                                        .id(w.getId())
                                        .authorName(w.getAuthorName())
                                        .genre(w.getGenre())
                                        .thumbnail(w.getThumbnail())
                                        .starRating(w.getStarRating())
                                        .dayOfWeek(w.getDayOfWeek())
                                        .name(w.getName())
                                        .build())
                                .toList())
                .next(findNext(webtoonList.get(webtoonList.size() - 1), order))
                .build();
    }

    private static String findNext(WebtoonListResultDto webtoon, String order) {
        if (order.equals("인기순")) {
            return webtoon.getPopularityScore().toString();
        } else if (order.equals("별점순")) {
            return webtoon.getStarRating().toString();
        }

        return webtoon.getPopularityScore().toString();
    }
}
