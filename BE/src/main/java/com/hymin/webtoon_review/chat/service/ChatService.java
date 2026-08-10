package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.exception.ChatRoomNotFoundException;
import com.hymin.webtoon_review.chat.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.repository.ChatRoomRepository;
import com.hymin.webtoon_review.chat.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.chat.repository.projection.ChatRoomGroup;
import com.hymin.webtoon_review.chat.repository.projection.ChatRoomStatistics;
import com.hymin.webtoon_review.chat.repository.projection.LastReadMessageSequenceGroup;
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

    public ChatRoom get(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(ChatRoomNotFoundException::new);
    }

    public Optional<ChatRoom> getByWebtoonId(Long webtoonId) {
        return chatRoomRepository.findByWebtoonId(webtoonId);
    }

    public List<ChatRoomGroup> getChatRoomGroups(Long userId) {
        return chatRoomRepository.findChatRoomGroups(userId);
    }

    public ChatRoomStatistics getChatRoomStatistics(Long roomId) {
        return userChatRoomRepository.findRoomStatistics(roomId);
    }

    public List<LastReadMessageSequenceGroup> getLastReadMessageSequenceGroup(Long roomId) {
        return userChatRoomRepository.findLastReadSequenceGroups(roomId);
    }

    public void joinChatRoom(UserChatRoom userChatRoom) {
        userChatRoomRepository.save(userChatRoom);
    }

    public Boolean existsRoomByUsername(Long roomId, String username) {
        return userChatRoomRepository.existsByChatRoomIdAndUserUsername(roomId, username);
    }

    public String getRoomMemberId(Long userId, Long roomId) {
        return userChatRoomRepository.findRoomMemberId(userId, roomId)
            .orElseThrow(InvalidChatRoomAccessException::new);
    }

    @Transactional
    public void connect(Long userId, Long chatRoomId) {
        userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
            .connect();
    }

    @Transactional
    public void disconnect(Long userId, Long chatRoomId) {
        userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
            .disconnect();
    }

    public void save(ChatRoom chatRoom) {
        chatRoomRepository.save(chatRoom);
    }
}
