package com.hymin.webtoon_review.file.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class UploadFileNotFoundException extends GeneralException {

    public UploadFileNotFoundException() {
        super(ResponseStatus.UPLOAD_FILE_NOT_FOUND);
    }
}
