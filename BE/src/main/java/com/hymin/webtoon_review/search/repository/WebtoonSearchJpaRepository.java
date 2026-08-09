package com.hymin.webtoon_review.search.repository;

import com.hymin.webtoon_review.search.entity.WebtoonSearch;
import com.hymin.webtoon_review.search.repository.projection.WebtoonSearchResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WebtoonSearchJpaRepository extends JpaRepository<WebtoonSearch, Long> {

    @Query(value = """
        SELECT webtoon_id FROM (
            (SELECT /*+ index(webtoon_search idx_webtoon_search_2) */ webtoon_id, priority
            FROM webtoon_search
            WHERE priority IN (1, 2, 3, 4, 5) AND title_part LIKE :query LIMIT 1000)
            UNION ALL
            (SELECT /*+ index(webtoon_search idx_webtoon_search) */ webtoon_id, priority
            FROM webtoon_search
            WHERE priority > 5 AND title_part LIKE :query LIMIT 1000) LIMIT 1000
        ) t GROUP BY webtoon_id ORDER BY MIN(priority) LIMIT :offset, :size;
        """, nativeQuery = true)
    List<Long> findWebtoonIds(String query, Integer offset, Integer size);

    @Query(value = """
        SELECT id, MIN(priority) priority, title_part, webtoon_id FROM (
            (SELECT /*+ index(webtoon_search idx_webtoon_search_2) */ *
            FROM webtoon_search
            WHERE priority IN (1, 2, 3, 4, 5) AND title_part LIKE :query LIMIT 1000)
            UNION ALL
            (SELECT /*+ index(webtoon_search idx_webtoon_search) */ *
            FROM webtoon_search
            WHERE priority > 5 AND title_part LIKE :query LIMIT 1000) LIMIT 1000
        ) t group by webtoon_id, id, title_part LIMIT :offset, :size;
        """, nativeQuery = true)
    List<WebtoonSearch> findWebtoonSearch(String query, Integer offset, Integer size);

    @Query(value = """
        SELECT id, name, thumbnail, authors
        FROM webtoon
        WHERE id IN :ids;
        """, nativeQuery = true
    )
    List<WebtoonSearchResult> findWebtoonSearchResult(List<Long> ids);
}
