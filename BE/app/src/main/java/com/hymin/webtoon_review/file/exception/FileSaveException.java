package com.hymin.webtoon_review.file.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class FileSaveException extends GeneralException {

    public FileSaveException() {
        super(ResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
