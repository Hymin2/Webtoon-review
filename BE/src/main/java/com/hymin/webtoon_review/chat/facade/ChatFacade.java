package com.hymin.webtoon_review.chat.facade;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatRoomListResponse;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatRoomResponse;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.service.ChatMessageRoutingService;
import com.hymin.webtoon_review.chat.service.ChatService;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final UserService userService;
    private final ChatService chatService;
    private final ChatMessageRoutingService chatMessageRoutingService;
    private final WebtoonService webtoonService;

    public void sendMessage(ChatMessageRequest chatMessage, Long userId, String nickname) {
        log.info("[채팅] 채팅 메시지 수신 완료, {}", chatMessage.toString());

        chatMessageRoutingService.route(
            ChatMapper.toChatMessageDto(
                chatMessage,
                userId,
                nickname
            )
        );
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
    public void joinChatRoom(Long webtoonId, String username) {
        User user = userService.get(username);
        Optional<ChatRoom> chatRoom = chatService.getByWebtoonId(webtoonId);

        if (chatRoom.isEmpty()) {
            Webtoon webtoon = webtoonService.get(webtoonId);
            ChatRoom newChatRoom = ChatRoom.builder()
                .webtoon(webtoon)
                .name(webtoon.getName())
                .build();

            chatService.save(newChatRoom);
            chatService.joinChatRoom(ChatMapper.toUserChatRoom(user, newChatRoom));
        } else if (!chatService.existsRoomByUsername(chatRoom.get().getId(), user.getUsername())) {
            chatService.joinChatRoom(ChatMapper.toUserChatRoom(user, chatRoom.get()));
        } else {
            throw new InvalidChatRoomAccessException();
        }
    }
}
