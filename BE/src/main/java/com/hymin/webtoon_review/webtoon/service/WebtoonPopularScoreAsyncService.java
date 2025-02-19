package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.async.AsyncProcessor;
import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.webtoon.dto.WebtoonPopularityScore;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebtoonPopularScoreAsyncService implements AsyncProcessor {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void process(List<Job<?>> jobs) {
        List<WebtoonPopularityScore> webtoonPopularityScores = jobs
            .stream()
            .map(job -> (WebtoonPopularityScore) job.getData())
            .toList();

        updatePopularityScore(webtoonPopularityScores);
    }

    private void updatePopularityScore(List<WebtoonPopularityScore> webtoonPopularityScores) {
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
}
