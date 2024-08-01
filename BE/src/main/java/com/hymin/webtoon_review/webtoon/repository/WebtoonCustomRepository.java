package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface WebtoonCustomRepository {

    List<WebtoonInfo> getWebtoons(Pageable pageable, String name, List<String> dayOfWeek,
        List<String> platform,
        List<String> genre);
}
