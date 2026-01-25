package com.hymin.webtoon_review.user.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class RefreshTokenReuseException extends GeneralException {

    public RefreshTokenReuseException() {
        super(ResponseStatus.REFRESH_TOKEN_REUSE);
    }
}
