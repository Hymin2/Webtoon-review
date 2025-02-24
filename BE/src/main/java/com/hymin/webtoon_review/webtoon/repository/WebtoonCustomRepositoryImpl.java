package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.user.entity.QBookmark.bookmark;
import static com.hymin.webtoon_review.user.entity.QUser.user;
import static com.hymin.webtoon_review.user.entity.QWebtoonRecommend.webtoonRecommend;
import static com.hymin.webtoon_review.webtoon.entity.QAuthor.author;
import static com.hymin.webtoon_review.webtoon.entity.QDayOfWeek.dayOfWeek;
import static com.hymin.webtoon_review.webtoon.entity.QGenre.genre;
import static com.hymin.webtoon_review.webtoon.entity.QPlatform.platform;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonAuthor.webtoonAuthor;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonDayOfWeek.webtoonDayOfWeek;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonGenre.webtoonGenre;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.hymin.webtoon_review.webtoon.entity.QGenre;
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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.QueryHints;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WebtoonSimple> getWebtoonList(
        Pageable pageable,
        String lastValue,
        String daysOfWeek,
        String genre,
        String updatedAt) {
        JPAQuery<WebtoonSimple> query = jpaQueryFactory
            .select(Projections.constructor(WebtoonSimple.class,
                webtoon.id,
                webtoon.name,
                webtoon.thumbnail,
                webtoon.recommendationCount,
                webtoon.totalStarScore,
                webtoon.totalPopularityScore,
                webtoon.manPopularityScore,
                webtoon.femalePopularityScore,
                webtoon.updatedAt
            ))
            .distinct()
            .from(webtoon);

        if (daysOfWeek != null) {
            query
                .join(webtoon.webtoonDayOfWeeks, webtoonDayOfWeek)
                .join(webtoonDayOfWeek.dayOfWeek, dayOfWeek)
                .on(dayOfWeek.name.stringValue().eq(daysOfWeek));
        }

        if (genre != null) {
            query
                .join(webtoon.webtoonGenres, webtoonGenre)
                .join(webtoonGenre.genre, QGenre.genre)
                .on(QGenre.genre.name.eq(genre));
        }

        return query
            .where(getLastValueCondition(pageable.getSort(), lastValue),
                getUpdatedAtCondition(updatedAt))
            .orderBy(toOrderSpecifier(pageable.getSort()))
            .limit(pageable.getPageSize() + 1)
            .setHint(QueryHints.COMMENT, "straight_join")
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
                webtoon.updatedAt))
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
                webtoon.thumbnail.stringValue(),
                webtoon.description,
                platform.name,
                webtoon.views,
                webtoon.totalStarScore,
                webtoon.manStarScore,
                webtoon.femaleStarScore,
                webtoon.recommendationCount,
                getSubQueryAboutIsRecommended(username),
                getSubQueryAboutIsBookmarked(username)
            ))
            .from(webtoon)
            .where(webtoon.id.eq(webtoonId))
            .join(webtoon.platform, platform)
            .fetchOne();
    }

    @Override
    public List<DayOfWeekSelectResult> getDayOfWeek(List<Long> webtoonId) {
        return jpaQueryFactory
            .select(Projections.constructor(DayOfWeekSelectResult.class,
                webtoon.id,
                dayOfWeek.name.stringValue()))
            .from(webtoonDayOfWeek)
            .join(webtoonDayOfWeek.webtoon, webtoon)
            .on(webtoon.id.in(webtoonId))
            .join(webtoonDayOfWeek.dayOfWeek, dayOfWeek)
            .orderBy(dayOfWeek.id.asc())
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

    private BooleanExpression getUpdatedAtCondition(String updatedAt) {
        if (updatedAt == null) {
            return null;
        }

        return webtoon.updatedAt.stringValue().gt(updatedAt);
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
            return webtoon.updatedAt.gt(LocalDateTime.parse(lastValue));
        } else if (property.equals("최신순") && order.isDescending()) {
            return webtoon.updatedAt.lt(LocalDateTime.parse(lastValue));
        } else if (property.equals("인기순") && order.isAscending()) {
            return webtoon.totalPopularityScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("인기순") && order.isDescending()) {
            return webtoon.totalPopularityScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("남성 인기순") && order.isAscending()) {
            return webtoon.manPopularityScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("남성 인기순") && order.isDescending()) {
            return webtoon.manPopularityScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("여성 인기순") && order.isAscending()) {
            return webtoon.femalePopularityScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("여성 인기순") && order.isDescending()) {
            return webtoon.femalePopularityScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("별점순") && order.isAscending()) {
            return webtoon.totalStarScore.gt(Integer.valueOf(lastValue));
        } else if (property.equals("별점순") && order.isDescending()) {
            return webtoon.totalStarScore.lt(Integer.valueOf(lastValue));
        } else if (property.equals("추천순") && order.isAscending()) {
            return webtoon.recommendationCount.gt(Integer.valueOf(lastValue));
        } else if (property.equals("추천순") && order.isDescending()) {
            return webtoon.recommendationCount.lt(Integer.valueOf(lastValue));
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
                            new OrderSpecifier<>(direction, webtoon.totalPopularityScore));
                        break;
                    case "남성 인기순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoon.manPopularityScore));
                        break;
                    case "여성 인기순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoon.femalePopularityScore));
                        break;
                    case "별점순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoon.totalStarScore));
                        break;
                    case "추천순":
                        orderSpecifiers.add(
                            new OrderSpecifier<>(direction, webtoon.recommendationCount));
                        break;
                    case "최신순":
                        orderSpecifiers.add(new OrderSpecifier<>(direction, webtoon.updatedAt));
                        break;
                }
            });

        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }
}
