package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.exception.ChatRoomNotFoundException;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.repository.ChatRoomRepository;
import com.hymin.webtoon_review.chat.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    public List<UserChatRoom> getUserChatRooms(Long roomId) {
        return userChatRoomRepository.findByChatRoomId(roomId);
    }

    public ChatRoom get(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(ChatRoomNotFoundException::new);
    }

    public void joinChatRoom(User user, ChatRoom chatRoom) {
        userChatRoomRepository.save(ChatMapper.toUserChatRoom(user, chatRoom));
    }

    public Boolean existsRoomByUsername(Long roomId, String username) {
        return userChatRoomRepository.existsByChatRoomIdAndUserUsername(roomId, username);
    }
}
