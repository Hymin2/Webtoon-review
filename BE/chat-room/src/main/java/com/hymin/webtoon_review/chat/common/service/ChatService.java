package com.hymin.webtoon_review.chat.common.service;

import com.hymin.webtoon_review.chat.common.entity.ChatRoom;
import com.hymin.webtoon_review.chat.common.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.common.exception.ChatRoomNotFoundException;
import com.hymin.webtoon_review.chat.common.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.repository.ChatRoomRepository;
import com.hymin.webtoon_review.chat.common.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.chat.common.repository.projection.ChatRoomGroup;
import com.hymin.webtoon_review.chat.common.repository.projection.ChatRoomStatistics;
import com.hymin.webtoon_review.chat.common.repository.projection.LastReadMessageSequenceGroup;
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

    public UserChatRoom joinChatRoom(UserChatRoom userChatRoom) {
        return userChatRoomRepository.save(userChatRoom);
    }

    public Boolean existsRoomByUsername(Long roomId, String username) {
        return userChatRoomRepository.existsByChatRoomIdAndUserUsername(roomId, username);
    }

    public String getRoomMemberId(Long userId, Long roomId) {
        return userChatRoomRepository.findRoomMemberId(userId, roomId)
            .orElseThrow(InvalidChatRoomAccessException::new);
    }

    public UserChatRoom getUserChatRoom(Long userId, Long roomId) {
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(
            userId, roomId);
        if (userChatRoom == null) {
            throw new InvalidChatRoomAccessException();
        }
        return userChatRoom;
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
