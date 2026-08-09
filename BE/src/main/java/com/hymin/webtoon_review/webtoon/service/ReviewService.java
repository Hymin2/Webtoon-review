package com.hymin.webtoon_review.webtoon.service;

import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReviewRequest;
import com.hymin.webtoon_review.webtoon.entity.Review;
import com.hymin.webtoon_review.webtoon.exception.CommentNotFoundException;
import com.hymin.webtoon_review.webtoon.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRepository reviewRepository;

    public Review get(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new CommentNotFoundException(ResponseStatus.COMMENT_NOT_FOUND));
    }

    public void save(Long userId, Long webtoonId, ReviewRequest reviewRequest) {
        reviewRepository.save(userId, webtoonId, reviewRequest);
    }

    public void delete(Long userId, Long webtoonId, Long reviewId) {
        reviewRepository.delete(userId, webtoonId, reviewId);
    }

    public void like(Long userId, Long reviewId) {
        reviewRepository.like(userId, reviewId);
    }

    public void unlike(Long userId, Long reviewId, Long reviewLikeId) {
        reviewRepository.unlike(userId, reviewId, reviewLikeId);
    }
}
