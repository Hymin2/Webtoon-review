package com.hymin.webtoon_review.search.repository;

import static com.hymin.webtoon_review.search.entity.QWebtoonSearch.webtoonSearch;
import static com.hymin.webtoon_review.webtoon.entity.QWebtoon.webtoon;

import com.hymin.webtoon_review.search.dto.WebtoonSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WebtoonSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Long> findWebtoonByQuery(String query, Integer page) {
        Integer size = 20;
        Integer offset = (page - 1) * size;

        String sql = """
                SELECT webtoon_id FROM (
                    (SELECT /*+ index(webtoon_search idx_webtoon_search_2) */ webtoon_id, priority
                    FROM webtoon_search
                    WHERE priority IN (1, 2, 3, 4, 5) AND title_part LIKE ? LIMIT 1000)
                    UNION ALL
                    (SELECT /*+ index(webtoon_search idx_webtoon_search) */ webtoon_id, priority
                    FROM webtoon_search
                    WHERE priority > 5 AND title_part LIKE ? LIMIT 1000) LIMIT 1000
                ) t GROUP BY webtoon_id ORDER BY MIN(priority) LIMIT ?, ?;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("webtoon_id"),
                query + "%", query + "%", offset, size
        );
    }
}
