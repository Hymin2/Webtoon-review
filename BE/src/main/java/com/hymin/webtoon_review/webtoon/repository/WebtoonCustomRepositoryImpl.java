package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.user.entity.QBookmark.bookmark;
import static com.hymin.webtoon_review.user.entity.QUser.user;
import static com.hymin.webtoon_review.user.entity.QWebtoonRecommend.webtoonRecommend;
import static com.hymin.webtoon_review.webtoon.entity.QPlatform.platform;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonFilter.webtoonFilter;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    @Value("${domain.thumbnail}")
    private String domainThumbnail;

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WebtoonSimple> getWebtoonList(
        Pageable pageable,
        String lastValue,
        Optional<DayOfWeek> daysOfWeek,
        Optional<Genre> genre,
        String updatedAt) {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonSimple.class,
                webtoon.id,
                webtoon.name,
                webtoon.thumbnail.prepend(domainThumbnail),
                webtoon.recommendationCount,
                webtoon.totalStarScore,
                webtoon.totalPopularityScore,
                webtoon.manPopularityScore,
                webtoon.femalePopularityScore,
                webtoon.updatedAt.stringValue(),
                webtoon.authors,
                webtoon.dayOfWeeks,
                webtoon.genres
            ))
            .from(webtoonFilter)
            .innerJoin(webtoon)
            .on(webtoonFilter.webtoon.eq(webtoon))
            .where(
                getLastValueCondition(pageable.getSort(), lastValue),
                getDayOfWeekCondition(daysOfWeek),
                getGenreCondition(genre))
            .orderBy(toOrderSpecifier(pageable.getSort()))
            .limit(pageable.getPageSize() + 1)
            .fetch();
    }

    @Override
    public List<WebtoonSimple> getHotWebtoonList() {
        return jpaQueryFactory
            .select(Projections.constructor(WebtoonSimple.class,
                webtoon.id,
                webtoon.name,
                webtoon.thumbnail,
                webtoon.recommendationCount,
                webtoon.totalStarScore,
                webtoon.totalPopularityScore,
                webtoon.manPopularityScore,
                webtoon.femalePopularityScore,
                webtoon.updatedAt.stringValue(),
                webtoon.authors,
                webtoon.dayOfWeeks,
                webtoon.genres))
            .from(webtoon)
            .orderBy(webtoon.totalPopularityScore.desc())
            .limit(30l)
            .fetch();
    }

    @Override
    public WebtoonDetails getWebtoon(String username, Long webtoonId) {
        return jpaQueryFactory
            .selectDistinct(Projections.constructor(WebtoonDetails.class,
                webtoon.id,
                webtoon.name,
                webtoon.thumbnail.prepend(domainThumbnail),
                webtoon.description,
                platform.name,
                webtoon.views,
                webtoon.recommendationCount,
                webtoon.totalStarScore,
                webtoon.manStarScore,
                webtoon.femaleStarScore,
                getSubQueryAboutIsRecommended(username),
                getSubQueryAboutIsBookmarked(username),
                webtoon.authors,
                webtoon.dayOfWeeks,
                webtoon.genres
            ))
            .from(webtoon)
            .where(webtoon.id.eq(webtoonId))
            .join(webtoon.platform, platform)
            .fetchOne();
    }

    @Override
    public void updateViews(List<Long> webtoonIdList) {
        String sql = "UPDATE webtoon SET views = views + 1 WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, webtoonIdList, webtoonIdList.size(), (ps, id) -> {
            ps.setLong(1, id);
        });
    }

    @Override
    public void updatePopularityScore(List<WebtoonPopularityScore> webtoonPopularityScores) {
        String sql = "UPDATE webtoon "
            + "SET total_popularity_score = total_popularity_score + ?, "
            + "man_popularity_score = man_popularity_score + ?, "
            + "female_popularity_score = female_popularity_score + ? "
            + "WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, webtoonPopularityScores, webtoonPopularityScores.size(),
            (ps, webtoonPopularityScore) -> {
                ps.setInt(1, webtoonPopularityScore.getScore());
                ps.setInt(2, webtoonPopularityScore.getScore());
                ps.setInt(3, webtoonPopularityScore.getScore());
                ps.setLong(4, webtoonPopularityScore.getId());
            });
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

    private BooleanExpression getGenreCondition(Optional<Genre> genre) {
        return genre.map(webtoonFilter.genre::eq).orElse(null);

    }

    private BooleanExpression getDayOfWeekCondition(Optional<DayOfWeek> dayOfWeek) {
        return dayOfWeek.map(webtoonFilter.dayOfWeek::eq).orElse(null);
    }

    private BooleanExpression getLastValueCondition(Sort sort, String lastValue) {
        Sort.Order order = sort.stream()
            .findFirst()
            .orElse(null);

        if (order == null || lastValue == null || lastValue.isEmpty()) {
            return null;
        }

        String property = order.getProperty();

        if (property.equals("최신순") && order.isAscending()) {
            return webtoonFilter.webtoonCreatedAt.gt(LocalDateTime.parse(lastValue));
        } else if (property.equals("최신순") && order.isDescending()) {
            return webtoonFilter.webtoonCreatedAt.lt(LocalDateTime.parse(lastValue));
        } else if (property.equals("인기순") && order.isAscending()) {
            return webtoonFilter.totalPopularityScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("인기순") && order.isDescending()) {
            return webtoonFilter.totalPopularityScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("남성 인기순") && order.isAscending()) {
            return webtoonFilter.manPopularityScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("남성 인기순") && order.isDescending()) {
            return webtoonFilter.manPopularityScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("여성 인기순") && order.isAscending()) {
            return webtoonFilter.femalePopularityScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("여성 인기순") && order.isDescending()) {
            return webtoonFilter.femalePopularityScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("별점순") && order.isAscending()) {
            return webtoonFilter.totalStarScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("별점순") && order.isDescending()) {
            return webtoonFilter.totalStarScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("추천순") && order.isAscending()) {
            return webtoonFilter.recommendationCount.gt(Integer.valueOf(lastValue));
        } else if (property.equals("추천순") && order.isDescending()) {
            return webtoonFilter.recommendationCount.lt(Integer.valueOf(lastValue));
        }

        return null;
    }

    private OrderSpecifier<?>[] toOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        sort.stream()
            .forEach((order) -> {
                Order direction = Order.DESC;

                switch (order.getProperty()) {
                    case "인기순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoonFilter.totalPopularityScore));
                        break;
                    case "남성 인기순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoonFilter.manPopularityScore));
                        break;
                    case "여성 인기순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoonFilter.femalePopularityScore));
                        break;
                    case "별점순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoonFilter.totalStarScore));
                        break;
                    case "추천순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoonFilter.recommendationCount));
                        break;
                    case "최신순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoonFilter.webtoonCreatedAt));
                        break;
                }
            });

        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
