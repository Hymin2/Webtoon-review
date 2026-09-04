package com.hymin.webtoon_review.webtoon.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class InvalidWebtoonRecommendException extends GeneralException {

    public InvalidWebtoonRecommendException(
        ResponseStatus responseStatus) {
        super(responseStatus);
    }
}
