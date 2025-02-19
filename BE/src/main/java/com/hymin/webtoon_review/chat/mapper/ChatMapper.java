package com.hymin.webtoon_review.chat.mapper;

import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatReadCountInfo;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.user.entity.User;
import java.util.Map;

public class ChatMapper {

    public static UserChatRoom toUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
            .user(user)
            .chatRoom(chatRoom)
            .isConnected(false)
            .lastReadMessageId(0L)
            .build();
    }

    public static ChatReadCountInfo toChatReadCountInfo(Long roomId,
        Map<Object, Object> readCountMap) {
        return ChatReadCountInfo.builder()
            .roomId(roomId)
            .readCountMap(readCountMap)
            .build();
    }
}
