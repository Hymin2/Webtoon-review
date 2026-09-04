package com.hymin.webtoon_review.chat.server.facade;

import com.hymin.webtoon_review.chat.common.dto.ChatResponse.*;
import com.hymin.webtoon_review.chat.common.entity.*;
import com.hymin.webtoon_review.chat.common.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.mapper.ChatRoomMapper;
import com.hymin.webtoon_review.chat.common.service.ChatService;
import com.hymin.webtoon_review.user.entity.User;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.entity.Webtoon;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("chat")
@RequiredArgsConstructor
public class ChatRoomFacade {
    private final UserService userService;
    private final ChatService chatService;
    private final WebtoonService webtoonService;

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getRoomList(Long userId) {
        return ChatRoomMapper.toChatRoomList(chatService.getChatRoomGroups(userId));
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoomInfo(Long roomId, String username) {
        ChatRoom room = chatService.get(roomId);
        if (chatService.existsRoomByUsername(room.getId(), username)) {
            throw new InvalidChatRoomAccessException();
        }
        return ChatRoomMapper.toChatRoomInfo(room, chatService.getChatRoomStatistics(room.getId()),
                chatService.getLastReadMessageSequenceGroup(room.getId()));
    }

    @Transactional
    public ChatRoomJoinResponse joinChatRoom(Long webtoonId, String username) {
        User user = userService.get(username);
        Optional<ChatRoom> room = chatService.getByWebtoonId(webtoonId);
        UserChatRoom member;
        if (room.isEmpty()) {
            Webtoon webtoon = webtoonService.get(webtoonId);
            ChatRoom newRoom = ChatRoom.builder().webtoon(webtoon).name(webtoon.getName()).build();
            chatService.save(newRoom);
            member = chatService.joinChatRoom(ChatRoomMapper.toUserChatRoom(user, newRoom));
        } else if (!chatService.existsRoomByUsername(room.get().getId(), user.getUsername())) {
            member = chatService.joinChatRoom(ChatRoomMapper.toUserChatRoom(user, room.get()));
        } else {
            member = chatService.getUserChatRoom(user.getId(), room.get().getId());
        }
        return ChatRoomMapper.toChatRoomJoinResponse(member);
    }
}
