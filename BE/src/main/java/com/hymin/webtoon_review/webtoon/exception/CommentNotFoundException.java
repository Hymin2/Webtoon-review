package com.hymin.webtoon_review.webtoon.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class CommentNotFoundException extends GeneralException {

    public CommentNotFoundException(
        ResponseStatus responseStatus) {
        super(responseStatus);
    }
}
