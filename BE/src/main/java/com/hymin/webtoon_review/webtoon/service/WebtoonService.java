package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.dto.WebtoonListResultDto;
import com.hymin.webtoon_review.webtoon.entity.Genre;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.exception.WebtoonNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.GenreRepository;
import com.hymin.webtoon_review.webtoon.repository.WebtoonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final JdbcTemplate jdbcTemplate;
    private final WebtoonRepository webtoonRepository;
    private final GenreRepository genreRepository;

    public List<WebtoonListResultDto> getWebtoonList(
            String order,
            String cursor,
            String daysOfWeek,
            String genre,
            String platform
    ) {
        return webtoonRepository.getWebtoonList(
                order,
                cursor,
                daysOfWeek,
                genre,
                platform
        );
    }

    public List<Genre> getAllGenre() {
        return genreRepository.findAll();
    }

    public Webtoon get(Long id) {
        return webtoonRepository.findById(id).orElseThrow(() -> new WebtoonNotFoundException(
                ResponseStatus.WEBTOON_NOT_FOUND));
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
}
