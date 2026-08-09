package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonListResultDto;
import java.util.List;

public interface WebtoonCustomRepository {

    List<WebtoonListResultDto> getWebtoonList(
            String order,
            String cursor,
            String daysOfWeek,
            String genre,
            String platform
    );
}
