package com.hymin.webtoon_review.chat.facade;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.teeing;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.JoinExitMessage;
import com.hymin.webtoon_review.chat.dto.ChatResponse.ChatReadCountInfo;
import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.service.ChatService;
import com.hymin.webtoon_review.chat.service.NotificationService;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final UserService userService;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final SimpMessageSendingOperations simpleMessageSendingOperations;

    public void sendMessage(ChatMessage chatMessage) {
        simpleMessageSendingOperations
            .convertAndSend("/topic/chat/" + chatMessage.getRoomId(), chatMessage);
        notificationService.send(chatMessage);
    }

    public void sendMessage(JoinExitMessage joinExitMessage) {
        simpleMessageSendingOperations
            .convertAndSend("/topic/chat/" + joinExitMessage.getRoomId(), joinExitMessage);
    }

    public ChatReadCountInfo getReadCount(Long roomId, String username) {
        if (chatService.existsRoomByUsername(roomId, username)) {
            throw new InvalidChatRoomAccessException();
        }

        return ChatMapper.toChatReadCountInfo(roomId,
            chatService.getUserChatRooms(roomId)
                .stream()
                .collect(teeing(
                    filtering(UserChatRoom::getIsConnected, counting()),
                    groupingBy(UserChatRoom::getLastReadMessageId, TreeMap::new,
                        filtering((item) -> !item.getIsConnected(), counting())),
                    (connectedUserCount, lastReadMessageIdToUserCountMap) ->
                        Map.of("connectedUserCount", connectedUserCount,
                            "lastReadMessageIdToUserCountMap", lastReadMessageIdToUserCountMap)))
        );
    }

    public void joinChatRoom(Long roomId, String username) {
        User user = userService.get(username);
        ChatRoom chatRoom = chatService.get(roomId);

        chatService.joinChatRoom(user, chatRoom);
    }
}
