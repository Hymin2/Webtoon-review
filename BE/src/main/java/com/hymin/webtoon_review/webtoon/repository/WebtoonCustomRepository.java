package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface WebtoonCustomRepository {

    List<Webtoon> getWebtoonList(Pageable pageable,
        String lastValue,
        Optional<DayOfWeek> dayOfWeek,
        Optional<Genre> genre);

    List<Webtoon> getHotWebtoonList();

    WebtoonDetails getWebtoon(String username, Long webtoonId);

    void updateViews(List<Long> webtoonIdList);

    void updatePopularityScore(List<WebtoonPopularityScore> webtoonPopularityScores);


}
