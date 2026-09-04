package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.Category;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonListResponse;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.mapper.WebtoonMapper;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebtoonFacade {

    private final WebtoonService webtoonService;

    @Transactional(readOnly = true)
    public WebtoonListResponse getWebtoonList(
            String order,
            String cursor,
            String daysOfWeek,
            String genre,
            String platform
    ) {
        return WebtoonMapper.toWebtoonListResponse(
                webtoonService.getWebtoonList(order, cursor, daysOfWeek, genre, platform),
                order
        );
    }

    @Transactional(readOnly = true)
    public Webtoon getWebtoonDetails(Long userId, Long webtoonId) {
        return null;
    }

    @Transactional(readOnly = true)
    public List<Category> getWebtoonCategories() {
        return WebtoonMapper.toWebtoonCategoryList(
                webtoonService.getAllGenre()
        );
    }
}
