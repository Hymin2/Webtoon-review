package com.hymin.webtoon_review.chat.common;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class InvalidChatRoomAccessException extends GeneralException {

    public InvalidChatRoomAccessException() {
        super(ResponseStatus.INVALID_CHAT_ROOM_ACCESS);
    }
}
