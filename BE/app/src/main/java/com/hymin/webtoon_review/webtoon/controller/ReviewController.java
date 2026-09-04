package com.hymin.webtoon_review.webtoon.controller;

import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.RestResponse;
import com.hymin.webtoon_review.webtoon.dto.WebtoonRequest.ReviewRequest;
import com.hymin.webtoon_review.webtoon.facade.ReviewFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons/{webtoon_id}/reviews")
public class ReviewController {

    private final ReviewFacade reviewFacade;

    @PostMapping
    public RestResponse addReview(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @RequestBody ReviewRequest reviewRequest) {
        reviewFacade.addReview((Long) authentication.getDetails(), webtoonId, reviewRequest);
        return RestResponse.onCreated();
    }

    @DeleteMapping("/{review_id}")
    public RestResponse removeReview(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "review_id") Long reviewId
    ) {
        reviewFacade.removeReview((Long) authentication.getDetails(), webtoonId, reviewId);
        return RestResponse.noContent();
    }

    @PostMapping("{review_id}/like")
    public RestResponse addReviewLike(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "review_id") Long reviewId
    ) {
        reviewFacade.addReviewLike((Long) authentication.getDetails(), reviewId);
        return RestResponse.noContent();
    }

    @DeleteMapping("/{review_id}/like/{review_like_id}")
    public RestResponse removeReviewLike(
        @Auth Authentication authentication,
        @PathVariable(name = "webtoon_id") Long webtoonId,
        @PathVariable(name = "review_id") Long reviewId,
        @PathVariable(name = "review_like_id") Long reviewLikeId) {
        reviewFacade.removeReviewLike((Long) authentication.getDetails(), reviewId, reviewLikeId);
        return RestResponse.noContent();
    }
}
