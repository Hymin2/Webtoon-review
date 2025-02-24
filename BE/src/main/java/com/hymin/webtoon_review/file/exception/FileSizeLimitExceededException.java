package com.hymin.webtoon_review.file.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class FileSizeLimitExceededException extends GeneralException {

    public FileSizeLimitExceededException() {
        super(ResponseStatus.FILE_SIZE_LIMIT_EXCEEDED);
    }
}
