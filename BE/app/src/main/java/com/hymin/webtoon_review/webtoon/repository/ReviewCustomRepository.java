package com.hymin.webtoon_review.webtoon.repository;

import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReviewRequest;

public interface ReviewCustomRepository {

    void save(Long userId, Long webtoonId, ReviewRequest reviewRequest);

    void delete(Long userId, Long webtoonId, Long reviewId);

    void like(Long userId, Long reviewId);

    void unlike(Long userId, Long reviewId, Long reviewLikeId);
}
