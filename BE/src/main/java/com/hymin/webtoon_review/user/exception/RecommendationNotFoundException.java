package com.hymin.webtoon_review.user.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class RecommendationNotFoundException extends GeneralException {

    public RecommendationNotFoundException(
        ResponseStatus responseStatus) {
        super(responseStatus);
    }
}
