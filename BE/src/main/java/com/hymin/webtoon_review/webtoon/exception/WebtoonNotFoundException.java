package com.hymin.webtoon_review.webtoon.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class WebtoonNotFoundException extends GeneralException {

    public WebtoonNotFoundException(
        ResponseStatus responseStatus) {
        super(responseStatus);
    }
}
