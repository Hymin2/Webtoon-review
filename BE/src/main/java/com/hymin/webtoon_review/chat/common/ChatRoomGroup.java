package com.hymin.webtoon_review.chat.common;

public interface ChatRoomGroup {

    Long getRoomId();

    String getRoomName();

    String getRoomMemberId();

    String getLastMessage();

    String getLastMessageCreatedAt();
}
