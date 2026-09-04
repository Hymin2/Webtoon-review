package com.hymin.webtoon_review.chat.common.repository.projection;

public interface ChatRoomGroup {

    Long getRoomId();

    String getRoomName();

    String getRoomMemberId();

    String getLastMessage();

    String getLastMessageCreatedAt();
}
