package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonDetails;
import com.hymin.webtoon_review.webtoon.dto.WebtoonResponse.WebtoonSimple;
import com.hymin.webtoon_review.webtoon.entity.DayOfWeek;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.WebtoonNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.DayOfWeekRepository;
import com.hymin.webtoon_review.webtoon.repository.GenreRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public List<WebtoonSimple> getWebtoonList(Pageable pageable,
        String lastValue, Optional<DayOfWeek> daysOfWeek, Optional<Genre> genre, String updatedAt) {
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

    public Optional<DayOfWeek> getDayOfWeek(String name) {
        if (Objects.isNull(name)) {
            return Optional.empty();
        }
        return dayOfWeekRepository.findByName(name);
    }

    public Optional<Genre> getGenre(String name) {
        if (Objects.isNull(name)) {
            return Optional.empty();
        }
        return genreRepository.findByName(name);
    }

    public List<Genre> getAllGenre() {
        return genreRepository.findAll();
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
