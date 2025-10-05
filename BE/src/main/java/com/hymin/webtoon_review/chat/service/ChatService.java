package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.exception.ChatRoomNotFoundException;
import com.hymin.webtoon_review.chat.mapper.ChatMapper;
import com.hymin.webtoon_review.chat.repository.ChatRoomRepository;
import com.hymin.webtoon_review.chat.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.user.entity.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    public List<UserChatRoom> getUserChatRooms(Long roomId) {
        return userChatRoomRepository.findByChatRoomId(roomId);
    }

    public List<User> getConnectedUsers(Long roomId) {
        return userChatRoomRepository.findConnectedUserByRoomId(roomId);
    }

    public ChatRoom get(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(ChatRoomNotFoundException::new);
    }

    public Optional<ChatRoom> getByWebtoonId(Long webtoonId) {
        return chatRoomRepository.findByWebtoonId(webtoonId);
    }

    public void joinChatRoom(User user, ChatRoom chatRoom) {
        userChatRoomRepository.save(ChatMapper.toUserChatRoom(user, chatRoom));
    }

    public Boolean existsRoomByUsername(Long roomId, String username) {
        return userChatRoomRepository.existsByChatRoomIdAndUserUsername(roomId, username);
    }

    @Transactional
    public void connect(String nickname, Long chatRoomId) {
        userChatRoomRepository.findByUserNicknameAndChatRoomId(nickname, chatRoomId)
            .connect();
    }

    @Transactional
    public void disconnect(String nickname, Long chatRoomId) {
        userChatRoomRepository.findByUserNicknameAndChatRoomId(nickname, chatRoomId)
            .disconnect();
    }

    public void save(ChatRoom chatRoom) {
        chatRoomRepository.save(chatRoom);
    }
}
