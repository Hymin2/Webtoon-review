package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.user.entity.QBookmark.bookmark;
import static com.hymin.webtoon_review.user.entity.QUser.user;
import static com.hymin.webtoon_review.user.entity.QWebtoonRecommend.webtoonRecommend;
import static com.hymin.webtoon_review.webtoon.entity.QAuthor.author;
import static com.hymin.webtoon_review.webtoon.entity.QDayOfWeek.dayOfWeek1;
import static com.hymin.webtoon_review.webtoon.entity.QGenre.genre;
import static com.hymin.webtoon_review.webtoon.entity.QPlatform.platform;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonAuthor.webtoonAuthor;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonDayOfWeek.webtoonDayOfWeek;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonGenre.webtoonGenre;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.QueryHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WebtoonInfo> getWebtoons(
        String username,
        Pageable pageable,
        String name,
        String lastValue,
        List<String> daysOfWeek,
        List<String> platforms,
        List<String> genres) {
        BooleanExpression containsName = null;

        if (name != null && !name.isEmpty()) {
            containsName = webtoon.name.contains(name);
        }

        JPAQuery<WebtoonInfo> query = jpaQueryFactory
            .select(Projections.constructor(WebtoonInfo.class,
                webtoon.id,
                webtoon.name,
                webtoon.description,
                webtoon.thumbnail,
                platform.name,
                webtoon.updatedAt,
                webtoon.views,
                getSubQueryAboutRecommendedCount(),
                getSubQueryAboutIsRecommended(username),
                getSubQueryAboutIsBookmarked(username)
            ))
            .distinct()
            .from(webtoon);

        if (daysOfWeek != null) {
            query
                .join(webtoon.webtoonDayOfWeeks, webtoonDayOfWeek)
                .join(webtoonDayOfWeek.dayOfWeek, dayOfWeek1)
                .on(dayOfWeek1.dayOfWeek.stringValue().in(daysOfWeek));
        }

        if (genres != null) {
            query
                .join(webtoon.webtoonGenres, webtoonGenre)
                .join(webtoonGenre.genre, genre)
                .on(genre.name.in(genres));
        }

        if (platforms != null) {
            query
                .join(webtoon.platform, platform)
                .on(platform.name.in(platforms));
        } else {
            query
                .join(webtoon.platform, platform);
        }

        return query
            .where(getLastValueCondition(pageable.getSort(), lastValue), containsName)
            .orderBy(toOrderSpecifier(pageable.getSort()))
            .limit(pageable.getPageSize() + 1)
            .setHint(QueryHints.COMMENT, "straight_join")
            .fetch();
    }

    @Override
    public List<DayOfWeekSelectResult> getDayOfWeek(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(DayOfWeekSelectResult.class,
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
    public List<GenreSelectResult> getGenres(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(GenreSelectResult.class,
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
    public List<AuthorSelectResult> getAuthors(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(AuthorSelectResult.class,
                webtoon.id,
                author.name))
            .from(webtoonAuthor)
            .join(webtoonAuthor.webtoon, webtoon)
            .on(webtoon.id.in(webtoonId))
            .join(webtoonAuthor.author, author)
            .orderBy(author.name.asc())
            .fetch();
    }

    private JPQLQuery<Integer> getSubQueryAboutIsBookmarked(String username) {
        return JPAExpressions
            .selectOne()
            .from(bookmark)
            .join(user)
            .on(bookmark.webtoon.eq(webtoon), user.username.eq(username),
                bookmark.user.eq(user));
    }

    private JPQLQuery<Integer> getSubQueryAboutIsRecommended(String username) {
        return JPAExpressions
            .selectOne()
            .from(webtoonRecommend)
            .join(user)
            .on(webtoonRecommend.webtoon.eq(webtoon), user.username.eq(username),
                webtoonRecommend.user.eq(user));
    }

    private JPQLQuery<Integer> getSubQueryAboutRecommendedCount() {
        return JPAExpressions
            .select(webtoonRecommend.count().intValue())
            .from(webtoonRecommend)
            .where(webtoonRecommend.webtoon.eq(webtoon))
            .groupBy(webtoon.id);
    }

    private BooleanExpression getLastValueCondition(Sort sort, String lastValue) {
        Sort.Order order = sort.stream()
            .findFirst()
            .get();
        String property = order.getProperty();

        if (order == null || lastValue == null || lastValue.isEmpty()) {
            return null;
        }

        if (property.equals("updatedAt") && order.isAscending()) {
            return webtoon.updatedAt.gt(LocalDateTime.parse(lastValue));
        } else if (property.equals("updatedAt") && order.isDescending()) {
            return webtoon.updatedAt.lt(LocalDateTime.parse(lastValue));
        } else if (property.equals("name") && order.isAscending()) {
            return webtoon.name.gt(lastValue);
        } else if (property.equals("name") && order.isDescending()) {
            return webtoon.name.lt(lastValue);
        } else if (property.equals("views") && order.isAscending()) {
            return webtoon.views.gt(Integer.valueOf(lastValue));
        } else if (property.equals("views") && order.isDescending()) {
            return webtoon.views.lt(Integer.valueOf(lastValue));
        }

        return null;
    }

    private OrderSpecifier<?>[] toOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        sort.stream()
            .forEach((order) -> {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                switch (order.getProperty()) {
                    case "updatedAt":
                        orderSpecifiers.add(
                            new OrderSpecifier(direction, webtoon.updatedAt));
                        break;
                    case "name":
                        orderSpecifiers.add(new OrderSpecifier(direction, webtoon.name));
                        break;
                    case "views":
                        orderSpecifiers.add(new OrderSpecifier(direction, webtoon.views));
                        break;
                }
            });

        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
