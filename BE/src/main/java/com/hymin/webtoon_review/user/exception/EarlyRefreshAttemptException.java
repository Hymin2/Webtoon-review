package com.hymin.webtoon_review.user.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class EarlyRefreshAttemptException extends GeneralException {

    public EarlyRefreshAttemptException() {
        super(ResponseStatus.EARLY_REFRESH_ATTEMPT);
    }
}
