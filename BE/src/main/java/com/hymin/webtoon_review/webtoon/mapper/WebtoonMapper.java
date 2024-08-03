package com.hymin.webtoon_review.webtoon.mapper;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Author;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeek;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Genre;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Platform;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import java.util.List;

public class WebtoonMapper {

    public static List<WebtoonInfo> toWebtoonList(List<Webtoon> webtoonList,
        List<Platform> platformList, List<DayOfWeek> dayOfWeekList, List<Genre> genreList,
        List<Author> authorList) {
        return webtoonList.stream()
            .map((webtoon) ->
                WebtoonInfo.builder()
                    .id(webtoon.getId())
                    .name(webtoon.getName())
                    .description(webtoon.getDescription())
                    .thumbnail(webtoon.getThumbnail())
                    .platform(
                        platformList.stream()
                            .filter((platform) -> webtoon.getId().equals(platform.getWebtoonId()))
                            .map(Platform::getName)
                            .findFirst().get()
                    )
                    .dayOfWeeks(
                        dayOfWeekList.stream()
                            .filter((dayOfWeek) -> webtoon.getId().equals(dayOfWeek.getWebtoonId()))
                            .map(DayOfWeek::getName)
                            .toList()
                    )
                    .genres(
                        genreList.stream()
                            .filter((genre) -> webtoon.getId().equals(genre.getWebtoonId()))
                            .map(Genre::getName)
                            .toList()
                    )
                    .authorName(authorList.stream()
                        .filter((author) -> webtoon.getId().equals(author.getWebtoonId()))
                        .map(Author::getName)
                        .toList())
                    .build())
            .toList();
    }
}
