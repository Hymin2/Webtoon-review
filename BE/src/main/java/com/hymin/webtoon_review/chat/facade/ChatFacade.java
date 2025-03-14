package com.hymin.webtoon_review.chat.facade;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.ConnectDisConnectMessage;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatRoomInfo;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.service.ChatService;
import com.hymin.webtoon_review.chat.service.NotificationService;
import com.hymin.webtoon_review.global.async.Job;
import com.hymin.webtoon_review.global.async.JobQueue;
import com.hymin.webtoon_review.global.async.TopicNames;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final JobQueue jobQueue;
    private final UserService userService;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final SimpMessageSendingOperations simpleMessageSendingOperations;

    public void sendMessage(ChatMessage chatMessage) {
        simpleMessageSendingOperations
            .convertAndSend("/topic/chat/" + chatMessage.getRoomId(), chatMessage);
        notificationService.send(chatMessage);
        jobQueue.add(TopicNames.chat.name(), Job.of(chatMessage));
    }

    public void sendMessage(ConnectDisConnectMessage connectDisConnectMessage) {
        simpleMessageSendingOperations
            .convertAndSend("/topic/chat/" + connectDisConnectMessage.getRoomId(),
                connectDisConnectMessage);
    }

    public ChatRoomInfo getRoomInfoPrevConnect(Long roomId, String username) {
        if (chatService.existsRoomByUsername(roomId, username)) {
            throw new InvalidChatRoomAccessException();
        }

        return ChatMapper.toChatRoomInfo(roomId, chatService.getUserChatRooms(roomId));
    }

    public void joinChatRoom(Long roomId, String username) {
        User user = userService.get(username);
        ChatRoom chatRoom = chatService.get(roomId);

        chatService.joinChatRoom(user, chatRoom);
    }
}
