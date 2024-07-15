package com.hymin.webtoon_review.user.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class AlreadyUserExistsException extends GeneralException {

    public AlreadyUserExistsException(
        ResponseStatus responseStatus) {
        super(responseStatus);
    }
}
