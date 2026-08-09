package com.hymin.webtoon_review.webtoon.facade;

import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReviewRequest;
import com.hymin.webtoon_review.webtoon.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewService reviewService;

    @Transactional
    public void addReview(Long userId, Long webtoonId, ReviewRequest reviewRequest) {
        reviewService.save(userId, webtoonId, reviewRequest);
    }

    @Transactional
    public void removeReview(Long userId, Long webtoonId, Long reviewId) {
        reviewService.delete(userId, webtoonId, reviewId);
    }

    @Transactional
    public void addReviewLike(Long userId, Long reviewId) {
        reviewService.like(userId, reviewId);
    }

    @Transactional
    public void removeReviewLike(Long userId, Long reviewId, Long reviewLikeId) {
        reviewService.unlike(userId, reviewId, reviewLikeId);
    }
}
