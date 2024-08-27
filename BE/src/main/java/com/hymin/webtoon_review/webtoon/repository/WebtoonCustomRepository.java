package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface WebtoonCustomRepository {

    List<WebtoonInfo> getWebtoons(String username, Pageable pageable, String name,
        String lastValue, List<String> dayOfWeek,
        List<String> platform,
        List<String> genre);

    List<DayOfWeekSelectResult> getDayOfWeek(List<Long> webtoonId);

    List<GenreSelectResult> getGenres(List<Long> webtoonId);

    List<AuthorSelectResult> getAuthors(List<Long> webtoonId);
}
