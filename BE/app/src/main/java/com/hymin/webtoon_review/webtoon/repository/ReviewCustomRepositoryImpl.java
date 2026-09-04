package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Long userId, Long webtoonId, ReviewRequest reviewRequest) {
        String sql = "INSERT INTO review (content, score, status, user_id, webtoon_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, reviewRequest.getContent());
            ps.setInt(2, reviewRequest.getStarRating());
            ps.setBoolean(3, true);
            ps.setLong(4, userId);
            ps.setLong(5, webtoonId);
        });
    }

    @Override
    public void delete(Long userId, Long webtoonId, Long reviewId) {
        String sql = "DELETE FROM review WHERE user_id = ? AND webtoon_id = ? AND id = ?";
        jdbcTemplate.update(sql, ps -> {
            ps.setLong(1, userId);
            ps.setLong(2, webtoonId);
            ps.setLong(3, reviewId);
        });
    }

    @Override
    public void like(Long userId, Long reviewId) {
        String sql = "INSERT INTO review_like (user_id, review_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setLong(1, userId);
            ps.setLong(2, reviewId);
        });
    }

    @Override
    public void unlike(Long userId, Long reviewId, Long reviewLikeId) {
        String sql = "DELETE FROM review_like WHERE user_id = ? AND review_id = ? AND id = ?";
        jdbcTemplate.update(sql, ps -> {
            ps.setLong(1, userId);
            ps.setLong(2, reviewId);
            ps.setLong(3, reviewLikeId);
        });
    }
}
