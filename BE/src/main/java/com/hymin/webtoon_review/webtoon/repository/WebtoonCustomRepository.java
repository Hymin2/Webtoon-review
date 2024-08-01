package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Author;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeek;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Genre;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Platform;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface WebtoonCustomRepository {

    List<Webtoon> getWebtoons(Pageable pageable, String name, List<String> dayOfWeek,
        List<String> platform,
        List<String> genre);

    List<Platform> getPlatforms(List<Long> webtoonId);

    List<DayOfWeek> getDayOfWeek(List<Long> webtoonId);

    List<Genre> getGenres(List<Long> webtoonId);

    List<Author> getAuthors(List<Long> webtoonId);
}
