package com.hymin.webtoon_review.chat.common;

import com.hymin.webtoon_review.global.exception.GeneralException;
import com.hymin.webtoon_review.global.response.ResponseStatus;

public class ChatRoomNotFoundException extends GeneralException {

    public ChatRoomNotFoundException() {
        super(ResponseStatus.CHAT_ROOM_NOT_FOUND);
    }
}
