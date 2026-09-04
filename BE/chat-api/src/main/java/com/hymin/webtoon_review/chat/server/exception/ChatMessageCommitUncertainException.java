package com.hymin.webtoon_review.chat.server.exception;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class ChatMessageCommitUncertainException extends GeneralException {

    public ChatMessageCommitUncertainException() {
        super(ResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
