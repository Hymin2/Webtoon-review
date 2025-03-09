package com.hymin.webtoon_review.chat.mapper;

import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatRoomInfo;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.util.UUIDCompressor;
import java.util.Map;
import java.util.UUID;

public class ChatMapper {

    public static UserChatRoom toUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
            .user(user)
            .chatRoom(chatRoom)
            .isConnected(false)
            .build();
    }

    public static ChatRoomInfo toChatRoomInfo(Long roomId,
        Map<Object, Object> readCountMap) {
        return ChatRoomInfo.builder()
            .roomId(roomId)
            .readCountMap(readCountMap)
            .personalUUID(UUIDCompressor.encode(UUID.randomUUID()))
            .build();
    }
}
