package com.hymin.webtoon_review.chat.mapper;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.teeing;

import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatRoomInfo;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ChatMapper {

    public static UserChatRoom toUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
            .user(user)
            .chatRoom(chatRoom)
            .isConnected(false)
            .build();
    }

    public static ChatRoomInfo toChatRoomInfo(Long roomId, List<UserChatRoom> userChatRooms) {
        return ChatRoomInfo.builder()
            .roomId(roomId)
            .readCountMap(
                userChatRooms.stream()
                    .collect(teeing(
                        filtering(UserChatRoom::getIsConnected, counting()),
                        groupingBy(UserChatRoom::getLastReadMessageCreatedAt, TreeMap::new,
                            filtering((item) -> !item.getIsConnected(), counting())),
                        (connectedUserCount, lastReadMessageIdToUserCountMap) ->
                            Map.of("connectedUserCount", connectedUserCount,
                                "lastReadMessageCreatedAtToUserCountMap",
                                lastReadMessageIdToUserCountMap)))
            )
            .personalUUID(UUID.randomUUID().toString())
            .build();
    }
}
