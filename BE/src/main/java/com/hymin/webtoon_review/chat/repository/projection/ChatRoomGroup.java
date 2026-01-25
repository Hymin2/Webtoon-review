package com.hymin.webtoon_review.chat.repository.projection;

public interface ChatRoomGroup {

    Long getRoomId();

    String getRoomName();

    String getLastMessage();

    String getLastMessageCreatedAt();
}
