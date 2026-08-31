package com.hymin.webtoon_review.chat.server.facade;

import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatMessageResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomJoinResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomListResponse;
import com.hymin.webtoon_review.chat.common.dto.ChatResponse.ChatRoomResponse;
import com.hymin.webtoon_review.chat.common.entity.ChatRoom;
import com.hymin.webtoon_review.chat.common.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.common.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.server.service.ChatMessageRoutingService;
import com.hymin.webtoon_review.chat.server.service.ChatMessageQueryService;
import com.hymin.webtoon_review.chat.server.service.ChatServerMessageIdService;
import com.hymin.webtoon_review.chat.server.service.ChatService;
import com.hymin.webtoon_review.global.manager.TraceContextManager;
import com.hymin.webtoon_review.global.manager.TraceContextManager.TraceScope;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import io.micrometer.tracing.Tracer;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("chat")
@RequiredArgsConstructor
public class ChatFacade {

    private final Tracer tracer;
    private final TraceContextManager traceManager;

    private final UserService userService;
    private final ChatService chatService;
    private final ChatMessageRoutingService chatMessageRoutingService;
    private final ChatMessageQueryService chatMessageQueryService;
    private final ChatServerMessageIdService chatServerMessageIdService;
    private final WebtoonService webtoonService;

    public void sendMessage(ChatMessageRequest chatMessage, Long userId, String nickname) {
        try (TraceScope scope = traceManager.startNewSpan("chat-message-receive")) {
            String traceId = scope.span().context().traceId();
            traceManager.putChatMDC(chatMessage.getRoomId(), userId);
            log.info("[채팅] 채팅 메시지 수신 완료");

            String roomMemberId = chatService.getRoomMemberId(
                userId, chatMessage.getRoomId());
            String messageId = chatServerMessageIdService.getOrCreate(
                chatMessage.getRoomId(),
                userId,
                chatMessage.getClientMessageId()
            );

            chatMessageRoutingService.route(
                    ChatMapper.toChatMessageDto(
                            chatMessage,
                            userId,
                            nickname,
                            roomMemberId,
                            traceId,
                            messageId
                    )
            );
        } finally {
            traceManager.removeChatMDC();
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getRoomList(Long userId) {
        return ChatMapper.toChatRoomList(chatService.getChatRoomGroups(userId));
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoomInfo(Long roomId, String username) {
        ChatRoom chatRoom = chatService.get(roomId);

        if (chatService.existsRoomByUsername(chatRoom.getId(), username)) {
            throw new InvalidChatRoomAccessException();
        }

        return ChatMapper.toChatRoomInfo(
                chatRoom,
                chatService.getChatRoomStatistics(chatRoom.getId()),
                chatService.getLastReadMessageSequenceGroup(chatRoom.getId())
        );
    }

    @Transactional
    public ChatRoomJoinResponse joinChatRoom(Long webtoonId, String username) {
        User user = userService.get(username);
        Optional<ChatRoom> chatRoom = chatService.getByWebtoonId(webtoonId);
        UserChatRoom userChatRoom;

        if (chatRoom.isEmpty()) {
            Webtoon webtoon = webtoonService.get(webtoonId);
            ChatRoom newChatRoom = ChatRoom.builder()
                    .webtoon(webtoon)
                    .name(webtoon.getName())
                    .build();

            chatService.save(newChatRoom);
            userChatRoom = chatService.joinChatRoom(
                ChatMapper.toUserChatRoom(user, newChatRoom));
        } else if (!chatService.existsRoomByUsername(chatRoom.get().getId(), user.getUsername())) {
            userChatRoom = chatService.joinChatRoom(
                ChatMapper.toUserChatRoom(user, chatRoom.get()));
        } else {
            userChatRoom = chatService.getUserChatRoom(user.getId(), chatRoom.get().getId());
        }

        return ChatMapper.toChatRoomJoinResponse(userChatRoom);
    }

    public List<ChatMessageResponse> getMessagesAfter(
        Long roomId,
        Long messageSequence,
        Long userId
    ) {
        chatService.getRoomMemberId(userId, roomId);
        return chatMessageQueryService.getMessagesAfter(roomId, messageSequence);
    }
}
