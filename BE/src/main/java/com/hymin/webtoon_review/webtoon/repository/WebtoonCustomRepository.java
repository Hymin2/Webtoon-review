package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface WebtoonCustomRepository {

    List<WebtoonSimple> getWebtoonList(Pageable pageable,
        String lastValue,
        String dayOfWeek,
        String genre,
        String updatedAt);

    List<WebtoonSimple> getHotWebtoonList();

    WebtoonDetails getWebtoon(String username, Long webtoonId);

    List<DayOfWeekSelectResult> getDayOfWeek(List<Long> webtoonId);

    List<GenreSelectResult> getGenres(List<Long> webtoonId);

    List<AuthorSelectResult> getAuthors(List<Long> webtoonId);
}
