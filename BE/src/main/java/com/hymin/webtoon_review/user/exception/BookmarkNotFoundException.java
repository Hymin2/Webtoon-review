package com.hymin.webtoon_review.user.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class BookmarkNotFoundException extends GeneralException {

    public BookmarkNotFoundException(
        ResponseStatus responseStatus) {
        super(responseStatus);
    }
}
