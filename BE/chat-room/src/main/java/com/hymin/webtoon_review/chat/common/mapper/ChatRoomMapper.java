package com.hymin.webtoon_review.chat.common.mapper;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomJoinResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomListResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomResponse;
import com.hymin.webtoon_review.chat.common.entity.ChatRoom;
import com.hymin.webtoon_review.chat.common.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.common.repository.projection.ChatRoomGroup;
import com.hymin.webtoon_review.chat.common.repository.projection.ChatRoomStatistics;
import com.hymin.webtoon_review.chat.common.repository.projection.LastReadMessageSequenceGroup;
import com.hymin.webtoon_review.user.entity.User;
import java.util.List;
import java.util.stream.Collectors;

public final class ChatRoomMapper {
    private ChatRoomMapper() {}

    public static UserChatRoom toUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder().user(user).chatRoom(chatRoom).isConnected(false).build();
    }

    public static ChatRoomJoinResponse toChatRoomJoinResponse(UserChatRoom member) {
        return ChatRoomJoinResponse.builder().roomId(member.getChatRoom().getId())
                .roomMemberId(member.getRoomMemberId()).build();
    }

    public static ChatRoomResponse toChatRoomInfo(ChatRoom room, ChatRoomStatistics statistics,
            List<LastReadMessageSequenceGroup> groups) {
        return ChatRoomResponse.builder().roomId(room.getId()).roomName(room.getName())
                .totalCount(statistics.getTotalCount()).onlineCount(statistics.getOnlineCount())
                .lastReadCountMap(groups.stream().collect(Collectors.toMap(
                        LastReadMessageSequenceGroup::getLastReadMessageSequence,
                        LastReadMessageSequenceGroup::getCount))).build();
    }

    public static List<ChatRoomListResponse> toChatRoomList(List<ChatRoomGroup> groups) {
        return groups.stream().map(c -> ChatRoomListResponse.builder().roomId(c.getRoomId())
                .roomName(c.getRoomName()).roomMemberId(c.getRoomMemberId())
                .lastChatMessage(c.getLastMessage())
                .lastChatMessageCreatedAt(c.getLastMessageCreatedAt()).build()).toList();
    }
}
