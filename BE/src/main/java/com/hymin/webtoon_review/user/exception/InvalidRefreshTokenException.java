package com.hymin.webtoon_review.user.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class InvalidRefreshTokenException extends GeneralException {

    public InvalidRefreshTokenException() {
        super(ResponseStatus.INVALID_TOKEN);
    }
}
