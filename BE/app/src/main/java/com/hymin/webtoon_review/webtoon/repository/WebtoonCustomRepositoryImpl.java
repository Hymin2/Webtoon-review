package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonFilter.webtoonFilter;

import com.hymin.webtoon_review.webtoon.dto.WebtoonFilterSubResultDto;
import com.hymin.webtoon_review.webtoon.dto.WebtoonListResultDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    @Value("${domain.thumbnail}")
    private String domainThumbnail;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WebtoonListResultDto> getWebtoonList(
            String order,
            String cursor,
            String daysOfWeek,
            String genre,
            String platform
    ) {
        List<WebtoonFilterSubResultDto> subResult = jpaQueryFactory.select(Projections.constructor(
                        WebtoonFilterSubResultDto.class,
                        webtoonFilter.webtoon.id,
                        webtoonFilter.starRating,
                        webtoonFilter.popularityScore)
                )
                .from(webtoonFilter)
                .where(
                        genreEq(genre), dayEq(daysOfWeek), platformEq(platform), cursorRange(cursor, order)
                )
                .groupBy(webtoonFilter.webtoon.id)
                .orderBy(subQueryOrder(order))
                .limit(22)
                .fetch();

        Map<Long, WebtoonListResultDto> mainResult = jpaQueryFactory.select(Projections.constructor(
                        WebtoonListResultDto.class,
                        webtoon.id,
                        webtoon.name,
                        webtoon.thumbnail.prepend(domainThumbnail),
                        webtoon.authors,
                        webtoon.dayOfWeeks,
                        webtoon.genres)
                )
                .from(webtoon)
                .where(webtoon.id.in(
                        subResult.stream().map(WebtoonFilterSubResultDto::getWebtoonId).toList())
                )
                .fetch().stream()
                .collect(Collectors.toMap(WebtoonListResultDto::getId, dto -> dto));

        return subResult.stream()
                .map(sub -> {
                    WebtoonListResultDto main = mainResult.get(sub.getWebtoonId());
                    main.setStarRating(sub.getStarRating());
                    main.setPopularityScore(sub.getPopularityScore());
                    return main;
                }).toList();
    }

    private BooleanExpression cursorRange(String cursor, String order) {
        if (cursor == null) {
            return null;
        }

        if (order.equals("인기순")) {
            return webtoonFilter.popularityScore.loe(Integer.valueOf(cursor));
        } else if (order.equals("별점순")) {
            return webtoonFilter.starRating.loe(Integer.valueOf(cursor));
        }

        return webtoonFilter.popularityScore.loe(Integer.valueOf(cursor));
    }

    private OrderSpecifier<Integer> subQueryOrder(String order) {
        if (order.equals("인기순")) {
            return webtoonFilter.popularityScore.max().desc();
        } else if (order.equals("별점순")) {
            return webtoonFilter.starRating.max().desc();
        }

        return webtoonFilter.popularityScore.max().desc();
    }

    private BooleanExpression genreEq(String genre) {
        return genre != null ? webtoonFilter.genre.name.eq(genre) : null;
    }

    private BooleanExpression dayEq(String dayOfWeek) {
        return dayOfWeek != null ? webtoonFilter.dayOfWeek.name.eq(dayOfWeek) : null;
    }

    private BooleanExpression platformEq(String platform) {
        return platform != null ? webtoonFilter.platform.name.eq(platform) : null;
    }
}
