package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.webtoon.entity.QAuthor.author;
import static com.hymin.webtoon_review.webtoon.entity.QDayOfWeek.dayOfWeek1;
import static com.hymin.webtoon_review.webtoon.entity.QGenre.genre;
import static com.hymin.webtoon_review.webtoon.entity.QPlatform.platform;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonAuthor.webtoonAuthor;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonDayOfWeek.webtoonDayOfWeek;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonGenre.webtoonGenre;

import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Author;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeek;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Genre;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.Platform;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Webtoon> getWebtoons(Pageable pageable, String name, List<String> daysOfWeek,
        List<String> platforms, List<String> genres) {
        BooleanExpression containsName;

        if (name == null || name.isEmpty()) {
            containsName = webtoon.name.contains("");
        } else {
            containsName = webtoon.name.contains(name);
        }

        return jpaQueryFactory
            .selectFrom(webtoon)
            .where(containsName
                .and(getSubQueryAboutDayOfWeek(daysOfWeek).exists())
                .and(getSubQueryAboutPlatform(platforms).exists())
                .and(getSubQueryAboutGenre(genres).exists()))
            .orderBy(toOrderSpecifier(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();
    }

    @Override
    public List<Platform> getPlatforms(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonSelectResult.Platform.class,
                webtoon.id,
                platform.name))
            .from(platform)
            .join(platform.webtoons, webtoon)
            .on(webtoon.id.in(webtoonId))
            .fetch();
    }

    @Override
    public List<DayOfWeek> getDayOfWeek(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonSelectResult.DayOfWeek.class,
                webtoon.id,
                dayOfWeek1.dayOfWeek.stringValue()))
            .from(webtoonDayOfWeek)
            .join(webtoonDayOfWeek.webtoon, webtoon)
            .on(webtoon.id.in(webtoonId))
            .join(webtoonDayOfWeek.dayOfWeek, dayOfWeek1)
            .orderBy(dayOfWeek1.id.asc())
            .fetch();
    }

    @Override
    public List<Genre> getGenres(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonSelectResult.Genre.class,
                webtoon.id,
                genre.name))
            .from(webtoonGenre)
            .join(webtoonGenre.webtoon, webtoon)
            .on(webtoon.id.in(webtoonId))
            .join(webtoonGenre.genre, genre)
            .orderBy(genre.name.asc())
            .fetch();
    }

    @Override
    public List<Author> getAuthors(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonSelectResult.Author.class,
                webtoon.id,
                author.name))
            .from(webtoonAuthor)
            .join(webtoonAuthor.webtoon, webtoon)
            .on(webtoon.id.in(webtoonId))
            .join(webtoonAuthor.author, author)
            .orderBy(author.name.asc())
            .fetch();
    }

    private JPQLQuery<Integer> getSubQueryAboutGenre(List<String> genres) {
        JPQLQuery<Integer> query = JPAExpressions
            .selectOne()
            .from(webtoonGenre)
            .join(webtoonGenre.genre, genre);

        if (genres == null || genres.isEmpty()) {
            return query
                .where(webtoonGenre.webtoon.eq(webtoon));
        } else {
            return query
                .on(genre.name.in(genres))
                .where(webtoonGenre.webtoon.eq(webtoon));
        }
    }

    private JPQLQuery<Integer> getSubQueryAboutPlatform(List<String> platforms) {
        JPQLQuery<Integer> query = JPAExpressions
            .selectOne()
            .from(platform);

        if (platforms == null || platforms.isEmpty()) {
            return query
                .where(webtoon.platform.eq(platform));
        } else {
            return query
                .where(platform.name.in(platforms).and(webtoon.platform.eq(platform)));
        }
    }

    private JPQLQuery<Integer> getSubQueryAboutDayOfWeek(List<String> daysOfWeek) {
        JPQLQuery<Integer> query = JPAExpressions
            .selectOne()
            .from(webtoonDayOfWeek)
            .innerJoin(webtoonDayOfWeek.dayOfWeek, dayOfWeek1);

        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return query
                .where(webtoonDayOfWeek.webtoon.eq(webtoon));
        } else {
            return query
                .on(dayOfWeek1.dayOfWeek.stringValue().in(daysOfWeek))
                .where(webtoonDayOfWeek.webtoon.eq(webtoon));
        }
    }

    private OrderSpecifier<?>[] toOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        sort.stream()
            .forEach((order) -> {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

                switch (order.getProperty()) {
                    case "updatedAt":
                        orderSpecifiers.add(new OrderSpecifier(direction, webtoon.updatedAt));
                        break;
                }
            });

        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
