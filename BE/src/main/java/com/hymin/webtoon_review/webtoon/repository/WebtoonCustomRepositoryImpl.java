package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.webtoon.entity.QAuthor.author;
import static com.hymin.webtoon_review.webtoon.entity.QDayOfWeek.dayOfWeek1;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonAuthor.webtoonAuthor;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonDayOfWeek.webtoonDayOfWeek;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonGenre.webtoonGenre;
import static com.querydsl.core.types.ExpressionUtils.list;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.entity.QGenre;
import com.hymin.webtoon_review.webtoon.entity.QPlatform;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
    public List<WebtoonInfo> getWebtoons(Pageable pageable, String name, List<String> dayOfWeek,
        List<String> platform, List<String> genre) {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonInfo.class,
                webtoon.id,
                webtoon.name,
                webtoon.description,
                webtoon.thumbnail,
                QPlatform.platform.name,
                list(String.class, author.name),
                list(String.class, dayOfWeek1.dayOfWeek.stringValue()),
                list(String.class, QGenre.genre.name.stringValue())))
            .from(webtoon)
            .innerJoin(webtoonAuthor)
            .on(webtoonAuthor.webtoon.eq(webtoon))
            .innerJoin(author)
            .on(webtoonAuthor.author.eq(author))
            .innerJoin(webtoonDayOfWeek)
            .on(webtoonDayOfWeek.webtoon.eq(webtoon))
            .innerJoin(dayOfWeek1)
            .on(dayOfWeek1.dayOfWeek.stringValue().in(dayOfWeek)
                .and(webtoonDayOfWeek.dayOfWeek.eq(dayOfWeek1)))
            .innerJoin(webtoonGenre)
            .on(webtoonGenre.webtoon.eq(webtoon))
            .innerJoin(QGenre.genre)
            .on(QGenre.genre.name.in(genre).and(webtoonGenre.genre.eq(QGenre.genre)))
            .where(webtoon.name.contains(name))
            .orderBy(toOrderSpecifier(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();
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
