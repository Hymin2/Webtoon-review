package com.hymin.webtoon_review.webtoon.repository;

import static com.hymin.webtoon_review.user.entity.QBookmark.bookmark;
import static com.hymin.webtoon_review.user.entity.QUser.user;
import static com.hymin.webtoon_review.user.entity.QWebtoonRecommend.webtoonRecommend;
import static com.hymin.webtoon_review.webtoon.entity.QAuthor.author;
import static com.hymin.webtoon_review.webtoon.entity.QDayOfWeek.dayOfWeek1;
import static com.hymin.webtoon_review.webtoon.entity.QGenre.genre;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonAuthor.webtoonAuthor;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonDayOfWeek.webtoonDayOfWeek;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoonGenre.webtoonGenre;

import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonInfo;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class WebtoonCustomRepositoryImpl implements WebtoonCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<WebtoonInfo> getWebtoons(
        String username,
        Pageable pageable,
        String name,
        String lastValue,
        List<String> daysOfWeek,
        List<String> platforms,
        List<String> genres) {
        StringBuilder rawQuery = new StringBuilder();

        rawQuery
            .append("SELECT DISTINCT")
            .append(
                " w.id, w.name, w.description, w.thumbnail, p.name as platform_name, w.views, w.updated_at, "
                    + "(SELECT count(*) FROM webtoon_recommend as wr WHERE wr.webtoon_id = w.id) as recommend_count, "
                    + "(SELECT 1 FROM webtoon_recommend as wr2 INNER JOIN user as u ON u.username = \""
                    + username
                    + "\" and wr2.user_id = u.id and wr2.webtoon_id = w.id) as is_recommended, "
                    + "(SELECT 1 FROM bookmark as b INNER JOIN user as u on u.username = \""
                    + username
                    + "\" and b.user_id = u.id and b.webtoon_id = w.id) as is_bookmarked")
            .append(" FROM webtoon as w")
            .append(" STRAIGHT_JOIN platform as p")
            .append(" ON w.platform_id = p.id");

        if (platforms != null) {
            rawQuery.append(" and p.name in (" + listToString(platforms) + ")");
        }

        if (genres != null) {
            rawQuery.append(" STRAIGHT_JOIN webtoon_genre as wg"
                + " ON wg.webtoon_id = w.id"
                + " STRAIGHT_JOIN genre as g"
                + " ON wg.genre_id = g.id and g.name in (" + listToString(genres) + ")");
        }

        if (daysOfWeek != null) {
            rawQuery.append(" STRAIGHT_JOIN webtoon_day_of_week as wdow"
                + " ON wdow.webtoon_id = w.id "
                + " STRAIGHT_JOIN day_of_week as dow"
                + " ON wdow.day_of_week_id = dow.id and dow.day_of_week in (" + listToString(
                daysOfWeek) + ")");
        }

        if (name != null) {
            rawQuery.append(" WHERE w.name LIKE \'%" + name + "%\'");
        }

        rawQuery
            .append(" ORDER BY " + sortToString(pageable.getSort()))
            .append(
                " limit " + pageable.getOffset() + "," + (Integer.valueOf(pageable.getPageSize())
                    + 1))
            .append(";");

        List<WebtoonInfo> webtoonInfoList = jdbcTemplate.query(rawQuery.toString(),
            (rs, rowNum) -> new WebtoonInfo(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("platform_name"),
                rs.getString("thumbnail"),
                rs.getInt("views"),
                rs.getInt("recommend_count"),
                rs.getInt("is_recommended"),
                rs.getInt("is_bookmarked")
            )
        );

//        BooleanExpression containsName = null;
//
//        if (name != null && name.isEmpty()) {
//            containsName = webtoon.name.contains(name);
//        }
//
//        JPAQuery<WebtoonInfo> query = jpaQueryFactory
//            .select(Projections.constructor(WebtoonInfo.class,
//                webtoon.id,
//                webtoon.name,
//                webtoon.description,
//                webtoon.thumbnail,
//                platform.name,
//                webtoon.views,
//                getSubQueryAboutRecommendedCount(),
//                getSubQueryAboutIsRecommended(username),
//                getSubQueryAboutIsBookmarked(username)
//            ))
//            .distinct()
//            .from(webtoon);
//
//        if (daysOfWeek != null) {
//            query
//                .innerJoin(webtoon.webtoonDayOfWeeks, webtoonDayOfWeek)
//                .innerJoin(webtoonDayOfWeek.dayOfWeek, dayOfWeek1)
//                .on(dayOfWeek1.dayOfWeek.stringValue().in(daysOfWeek));
//        }
//
//        if (genres != null) {
//            query
//                .innerJoin(webtoon.webtoonGenres, webtoonGenre)
//                .innerJoin(webtoonGenre.genre, genre)
//                .on(genre.name.in(genres));
//        }
//
//        if (platforms != null) {
//            query
//                .leftJoin(webtoon.platform, platform)
//                .on(platform.name.in(platforms));
//        } else {
//            query
//                .leftJoin(webtoon.platform, platform);
//        }
//
//        stopWatch.start();
//        query
//            .where(containsName)
//            .orderBy(toOrderSpecifier(pageable.getSort()))
//            .offset(pageable.getOffset())
//            .limit(pageable.getPageSize() + 1)
//            .fetch();
//        stopWatch.stop();
//
//        System.out.println(stopWatch.prettyPrint());
        return webtoonInfoList;
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
            .innerJoin(user)
            .on(bookmark.webtoon.eq(webtoon), user.username.eq(username),
                bookmark.user.eq(user));
    }

    private JPQLQuery<Integer> getSubQueryAboutIsRecommended(String username) {
        return JPAExpressions
            .selectOne()
            .from(webtoonRecommend)
            .innerJoin(user)
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

    private String camelToUnderscore(String input) {
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private String getSortColumn(Sort sort) {
        for (Sort.Order order : sort) {
            return order.getProperty();
        }

        return null;
    }

    private String sortToString(Sort sort) {
        return sort.stream()
            .map((s) -> camelToUnderscore(s.getProperty()) + " " + s.getDirection().name())
            .collect(Collectors.joining(", "));
    }

    private String listToString(List<String> stringList) {
        return stringList.stream()
            .map((str) -> "\"" + str + "\"")
            .collect(Collectors.joining(", "));
    }
}
