package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.hymin.webtoon_review.webtoon.entity.Author;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import java.util.List;
import java.util.stream.Collectors;

public class WebtoonMapper {

    public static List<WebtoonSimple> setWebtoonSimpleList(
        List<WebtoonSimple> webtoonSimpleList,
        List<DayOfWeekSelectResult> dayOfWeekSelectResultList,
        List<GenreSelectResult> genreSelectResultList,
        List<AuthorSelectResult> authorSelectResultList,
        String domainThumbnail
    ) {
        webtoonSimpleList
            .forEach((webtoon) -> {
                    webtoon.setDayOfWeek(dayOfWeekSelectResultList
                        .stream()
                        .filter((dow) -> webtoon.getId().equals(dow.getWebtoonId()))
                        .map(DayOfWeekSelectResult::getName)
                        .collect(Collectors.joining("·")));

                    webtoon.setGenre(genreSelectResultList
                        .stream()
                        .filter((item) -> webtoon.getId().equals(item.getWebtoonId()))
                        .map(GenreSelectResult::getName)
                        .collect(Collectors.joining("·")));

                    webtoon.setAuthorName(authorSelectResultList
                        .stream()
                        .filter((author) -> webtoon.getId().equals(author.getWebtoonId()))
                        .map(AuthorSelectResult::getName)
                        .collect(Collectors.joining("·")));

                    webtoon.addDomain(domainThumbnail);
                }
            );

        return webtoonSimpleList;
    }

    public static WebtoonDetails setWebtoonDetails(
        WebtoonDetails webtoonDetails,
        List<DayOfWeek> dayOfWeeks,
        List<Genre> genres,
        List<Author> authors,
        String domainThumbnail
    ) {
        webtoonDetails.setGenre(
            genres.stream()
                .map(Genre::getName)
                .collect(Collectors.joining("·"))
        );
        webtoonDetails.setDayOfWeek(
            dayOfWeeks.stream()
                .map((d) -> d.getName().name())
                .collect(Collectors.joining("·"))
        );
        webtoonDetails.setAuthorName(
            authors.stream()
                .map(Author::getName)
                .collect(Collectors.joining("·"))
        );
        webtoonDetails.addDomain(domainThumbnail);

        return webtoonDetails;
    }

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
