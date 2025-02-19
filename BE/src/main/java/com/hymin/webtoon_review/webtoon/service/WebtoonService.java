package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.AuthorSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.DayOfWeekSelectResult;
import com.hymin.webtoon_review.webtoon.dto.WebtoonSelectResult.GenreSelectResult;
import com.hymin.webtoon_review.webtoon.entity.Author;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.WebtoonNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.AuthorRepository;
import com.hymin.webtoon_review.webtoon.repository.DayOfWeekRepository;
import com.hymin.webtoon_review.webtoon.repository.GenreRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final JdbcTemplate jdbcTemplate;
    private final WebtoonRepository webtoonRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    public List<WebtoonSimple> getWebtoonList(Pageable pageable,
        String lastValue, String daysOfWeek, String genre, String updatedAt) {
        return webtoonRepository.getWebtoonList(
            pageable,
            lastValue,
            daysOfWeek,
            genre,
            updatedAt);
    }

    public List<WebtoonSimple> getHotWebtoonList() {
        return webtoonRepository.getHotWebtoonList();
    }

    public List<DayOfWeek> getDayOfWeeks(Long id) {
        return dayOfWeekRepository.findByWebtoonId(id);
    }

    public List<Genre> getGenres(Long id) {
        return genreRepository.findByWebtoonId(id);
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public List<Author> getAuthors(Long id) {
        return authorRepository.findByWebtoonId(id);
    }

    public List<DayOfWeekSelectResult> getDayOfWeekSelectResultList(List<Long> webtoonIdList) {
        return webtoonRepository.getDayOfWeek(webtoonIdList);
    }

    public List<GenreSelectResult> getGenreSelectResultList(List<Long> webtoonIdList) {
        return webtoonRepository.getGenres(webtoonIdList);
    }

    public List<AuthorSelectResult> getAuthorSelectResultList(List<Long> webtoonIdList) {
        return webtoonRepository.getAuthors(webtoonIdList);
    }

    public Webtoon get(Long id) {
        return webtoonRepository.findById(id).orElseThrow(() -> new WebtoonNotFoundException(
            ResponseStatus.WEBTOON_NOT_FOUND));
    }

    public WebtoonDetails get(String username, Long id) {
        return webtoonRepository.getWebtoon(username, id);
    }

    public void increaseCommentCount(Long id) {
        String sql =
            "UPDATE webtoon SET comment_count = comment_count + 1 WHERE webtoon_id = " + id;

        jdbcTemplate.update(sql);
    }

    public void decreaseCommentCount(Long id) {
        String sql =
            "UPDATE webtoon SET comment_count = comment_count - 1 WHERE webtoon_id = " + id;

        jdbcTemplate.update(sql);
    }

    public void increaseRecommendCount(Long id) {
        String sql =
            "UPDATE webtoon SET recommendation_count = recommendation_count + 1 WHERE webtoon_id = "
                + id;

        jdbcTemplate.update(sql);
    }

    public void decreaseRecommendCount(Long id) {
        String sql =
            "UPDATE webtoon SET recommendation_count = recommendation_count - 1 WHERE webtoon_id = "
                + id;

        jdbcTemplate.update(sql);
    }

    public void increaseStarScore(Long id, Double score) {
        int newScore = (int) (score * 10);

        String sql = "UPDATE webtoon "
            + "SET total_star_score = total_star_score + ?, "
            + "man_star_score = man_star_score + ?, "
            + "female_star_score = female_star_score + ? "
            + "WHERE webtoon_id = ?";

        jdbcTemplate.update(sql, (ps) -> {
            ps.setInt(1, newScore);
            ps.setInt(2, newScore);
            ps.setInt(3, newScore);
            ps.setLong(4, id);
        });
    }
}
