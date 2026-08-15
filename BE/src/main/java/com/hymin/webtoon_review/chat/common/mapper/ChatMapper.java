package com.hymin.webtoon_review.chat.common.mapper;

import com.hymin.webtoon_review.chat.common.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomJoinResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomListResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomResponse;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.entity.ChatRoom;
import com.hymin.webtoon_review.chat.common.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.common.repository.projection.ChatRoomGroup;
import com.hymin.webtoon_review.chat.common.repository.projection.ChatRoomStatistics;
import com.hymin.webtoon_review.chat.common.repository.projection.LastReadMessageSequenceGroup;
import com.hymin.webtoon_review.user.entity.User;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMapper {

    public static UserChatRoom toUserChatRoom(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .isConnected(false)
                .build();
    }

    public static ChatRoomJoinResponse toChatRoomJoinResponse(UserChatRoom userChatRoom) {
        return ChatRoomJoinResponse.builder()
            .roomId(userChatRoom.getChatRoom().getId())
            .roomMemberId(userChatRoom.getRoomMemberId())
            .build();
    }

    public static ChatMessageResponse toChatMessageResponse(
            ChatMessageDto chatMessageDto,
            Long messageSequence,
            String createdAt
    ) {
        return ChatMessageResponse.builder()
                .messageId(chatMessageDto.getMessageId())
                .roomId(chatMessageDto.getRoomId())
                .roomMemberId(chatMessageDto.getRoomMemberId())
                .clientMessageId(chatMessageDto.getClientMessageId())
                .messageBlocks(chatMessageDto.getMessageBlocks())
                .messageSequence(messageSequence)
                .createdAt(createdAt)
                .sender(chatMessageDto.getSenderNickname())
                .build();
    }

    public static ChatRoomResponse toChatRoomInfo(
            ChatRoom chatRoom,
            ChatRoomStatistics chatRoomStatistics,
            List<LastReadMessageSequenceGroup> lastReadMessageSequenceGroups
    ) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .totalCount(chatRoomStatistics.getTotalCount())
                .onlineCount(chatRoomStatistics.getOnlineCount())
                .lastReadCountMap(
                        lastReadMessageSequenceGroups.stream()
                                .collect(Collectors.toMap(
                                        LastReadMessageSequenceGroup::getLastReadMessageSequence,
                                        LastReadMessageSequenceGroup::getCount
                                ))
                ).build();
    }

    public static List<ChatRoomListResponse> toChatRoomList(List<ChatRoomGroup> chatRoomGroups) {
        return chatRoomGroups.stream()
                .map(c -> ChatRoomListResponse.builder()
                        .roomId(c.getRoomId())
                        .roomName(c.getRoomName())
                        .roomMemberId(c.getRoomMemberId())
                        .lastChatMessage(c.getLastMessage())
                        .lastChatMessageCreatedAt(c.getLastMessageCreatedAt())
                        .build()
                ).toList();
    }

    public static ChatMessage toChatMessage(ChatMessageResponse chatMessageResponse, Long senderId,
                                            String traceId) {
        return ChatMessage.builder()
                .id(chatMessageResponse.getMessageId())
                .roomId(chatMessageResponse.getRoomId())
                .senderId(senderId)
                .messageBlocks(chatMessageResponse.getMessageBlocks())
                .messageSequence(chatMessageResponse.getMessageSequence())
                .clientMessageId(chatMessageResponse.getClientMessageId())
                .traceId(traceId)
                .createdAt(chatMessageResponse.getCreatedAt())
                .build();
    }

    public static ChatMessageDto toChatMessageDto(
            ChatMessageRequest chatMessageRequest,
            Long userId,
            String nickname,
            String roomMemberId,
            String traceId,
            String messageId
    ) {
        return ChatMessageDto.builder()
                .messageId(messageId)
                .roomId(chatMessageRequest.getRoomId())
                .messageBlocks(chatMessageRequest.getMessageBlocks())
                .roomMemberId(roomMemberId)
                .clientMessageId(chatMessageRequest.getClientMessageId())
                .traceId(traceId)
                .senderId(userId)
                .senderNickname(nickname)
                .build();
    }
}
