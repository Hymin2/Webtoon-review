package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface WebtoonCustomRepository {

    List<WebtoonSimple> getWebtoonList(Pageable pageable,
        String lastValue,
        Optional<DayOfWeek> dayOfWeek,
        Optional<Genre> genre,
        String updatedAt);

    List<WebtoonSimple> getHotWebtoonList();

    WebtoonDetails getWebtoon(String username, Long webtoonId);

    void updateViews(List<Long> webtoonIdList);

    void updatePopularityScore(List<WebtoonPopularityScore> webtoonPopularityScores);
}
