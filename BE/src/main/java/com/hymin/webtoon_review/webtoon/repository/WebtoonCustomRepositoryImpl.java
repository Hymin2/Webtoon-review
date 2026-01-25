package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.user.entity.QBookmark.bookmark;
import static com.hymin.webtoon_review.user.entity.QUser.user;
import static com.hymin.webtoon_review.user.entity.QWebtoonRecommend.webtoonRecommend;
import static com.hymin.webtoon_review.webtoon.entity.QPlatform.platform;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;

import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.QWebtoon;
import com.hymin.webtoon_review.webtoon.entity.QWebtoonFilter;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.repository.enums.SortColumn;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    @Value("${domain.thumbnail}")
    private String domainThumbnail;

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory jpaQueryFactory;

    private final EntityManager entityManager;
    private final SQLTemplates mysqlTemplates;

    @Override
    public List<Webtoon> getWebtoonList(
        Pageable pageable,
        String lastValue,
        Optional<DayOfWeek> dayOfWeek,
        Optional<Genre> genre) {
        SortColumn sortColumn = pageable.getSort().stream()
            .findFirst()
            .map(o -> SortColumn.fromProperty(o.getProperty()))
            .orElse(SortColumn.TOTAL_POPULAR);

        JPASQLQuery<Webtoon> query = new JPASQLQuery<>(entityManager, mysqlTemplates);

        QWebtoon w = QWebtoon.webtoon;
        QWebtoonFilter wf = QWebtoonFilter.webtoonFilter;

        boolean hasFilter = dayOfWeek.isPresent() || genre.isPresent();

        query = query.select(w);

        if (hasFilter) {
            query = query.addFlag(QueryFlag.Position.AFTER_SELECT,
                " /*+ SEMIJOIN(@subq1 FIRSTMATCH) */ ");
        }

        query = query.from(w);

        BooleanExpression noOffset = getNoOffsetCondition(sortColumn, lastValue);
        BooleanExpression subSQL = SQLExpressions.selectOne()
            .from(wf)
            .addFlag(Position.AFTER_SELECT, " /*+ QB_NAME(subq1) */ ")
            .where(Expressions.numberPath(Long.class, wf, "webtoon_id").eq(w.id)
                .and(genre.map(g ->
                    Expressions.numberPath(Long.class, wf, "genre_id").eq(g.getId())
                ).orElse(null))
                .and(dayOfWeek.map(d ->
                    Expressions.numberPath(Long.class, wf, "day_of_week_id").eq(d.getId())
                ).orElse(null)))
            .exists();

        if (hasFilter) {
            query = query.where(noOffset, subSQL);
        } else {
            query = query.where(noOffset);
        }

        return query
            .orderBy(sortColumn.getExpression().desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();
    }

    @Override
    public List<Webtoon> getHotWebtoonList() {
        return jpaQueryFactory
            .selectFrom(webtoon)
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

    private BooleanExpression getNoOffsetCondition(SortColumn sortColumn, String lastValue) {
        if (lastValue == null) {
            return null;
        }

        return sortColumn.loe(lastValue);
    }
}
