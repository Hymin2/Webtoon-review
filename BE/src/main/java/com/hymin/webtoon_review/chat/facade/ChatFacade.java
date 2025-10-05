package com.hymin.webtoon_review.chat.facade;

import com.hymin.webtoon_review.chat.dto.ChatRequest.AckMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatRoomInfo;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.service.ChatService;
import com.hymin.webtoon_review.chat.service.MessageService;
import com.hymin.webtoon_review.chat.service.NotificationService;
import com.hymin.webtoon_review.chat.service.ReliableMessageService;
import com.hymin.webtoon_review.global.queue.Job;
import com.hymin.webtoon_review.global.queue.JobQueue;
import com.hymin.webtoon_review.global.queue.TopicNames;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final JobQueue jobQueue;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final ReliableMessageService reliableMessageService;
    private final WebtoonService webtoonService;

    public void receiveAck(String username, AckMessage ackMessage) {
        reliableMessageService.removeRetryMessage(username, ackMessage.getMessageUUID());
    }

    public void sendMessage(SimpMessageHeaderAccessor headerAccessor, ChatMessage chatMessage) {
        chatMessage.setSender((String) headerAccessor.getSessionAttributes().get("nickname"));

        switch (chatMessage.getType()) {
            case CONNECT -> chatService.connect(chatMessage.getSender(), chatMessage.getRoomId());
            case DISCONNECT ->
                chatService.disconnect(chatMessage.getSender(), chatMessage.getRoomId());
            default -> {
                notificationService.send(chatMessage);
                jobQueue.add(TopicNames.chat.name(), Job.of(chatMessage));
            }
        }

        broadcastMessage(chatMessage);
    }

    public void broadcastMessage(ChatMessage chatMessage) {
        messageService.send("/topic/chat/" + chatMessage.getRoomId(), chatMessage);
        reliableMessageService.addRetryMessage(
            chatService.getConnectedUsers(chatMessage.getRoomId()), chatMessage);
    }

    public ChatRoomInfo getRoomInfoPrevConnect(Long webtoonId, String username) {
        ChatRoom chatRoom = chatService.getByWebtoonId(webtoonId)
            .orElseThrow(InvalidChatRoomAccessException::new);

        if (chatService.existsRoomByUsername(chatRoom.getId(), username)) {
            throw new InvalidChatRoomAccessException();
        }

        return ChatMapper.toChatRoomInfo(
            chatRoom.getId(),
            chatService.getUserChatRooms(chatRoom.getId())
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
            chatService.joinChatRoom(user, newChatRoom);
        } else if (!chatService.existsRoomByUsername(chatRoom.get().getId(), user.getUsername())) {
            chatService.joinChatRoom(user, chatRoom.get());
        } else {
            throw new InvalidChatRoomAccessException();
        }
    }
}
