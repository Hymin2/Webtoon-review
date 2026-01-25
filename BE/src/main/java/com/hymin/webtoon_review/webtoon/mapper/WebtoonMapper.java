package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.webtoon.dto.WebtoonListResponseDto;
import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.HotWebtoonListResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonListResponse;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.repository.enums.SortColumn;
import java.util.List;
import org.springframework.data.domain.Pageable;

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

    public static WebtoonListResponseDto toWebtoonListResponseDto(
        List<Webtoon> webtoonList,
        String domainThumbnail,
        Pageable pageable
    ) {
        return WebtoonListResponseDto.builder()
            .webtoonListResponse(toWebtoonListResponse(webtoonList, domainThumbnail))
            .next(
                pageable.getSort().stream()
                    .findFirst()
                    .map(o -> SortColumn.fromProperty(o.getProperty()))
                    .orElse(SortColumn.TOTAL_POPULAR)
                    .extractScore(webtoonList.get(webtoonList.size() - 1)))
            .build();
    }

    public static List<WebtoonListResponse> toWebtoonListResponse(
        List<Webtoon> webtoonList,
        String domainThumbnail
    ) {
        return webtoonList.stream()
            .map(w -> WebtoonListResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .thumbnail(domainThumbnail + w.getThumbnail())
                .authorName(w.getAuthors())
                .dayOfWeek(w.getDayOfWeeks())
                .genre(w.getGenres())
                .starScore(w.getTotalStarScore())
                .build()
            ).toList();
    }

    public static List<HotWebtoonListResponse> toHotWebtoonListResponse(List<Webtoon> webtoonList,
        String domainThumbnail) {
        return webtoonList.stream()
            .map(w -> HotWebtoonListResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .thumbnail(domainThumbnail + w.getThumbnail())
                .build()
            ).toList();
    }
}
