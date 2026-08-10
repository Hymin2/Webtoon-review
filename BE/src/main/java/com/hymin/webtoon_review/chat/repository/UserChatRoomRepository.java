package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.chat.repository.projection.ChatRoomParticipantGroups;
import com.hymin.webtoon_review.chat.repository.projection.ChatRoomStatistics;
import com.hymin.webtoon_review.chat.repository.projection.LastReadMessageSequenceGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    Boolean existsByChatRoomIdAndUserUsername(Long roomId, String username);

    UserChatRoom findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT u.roomMemberId "
        + "FROM UserChatRoom u "
        + "WHERE u.user.id = :userId AND u.chatRoom.id = :roomId")
    Optional<String> findRoomMemberId(
        @Param("userId") Long userId,
        @Param("roomId") Long roomId
    );

    @Query("SELECT COUNT(u) AS totalCount, " +
        "COALESCE(SUM(CASE WHEN u.isConnected = true THEN 1 ELSE 0 END), 0) AS onlineCount " +
        "FROM UserChatRoom u " +
        "WHERE u.chatRoom.id = :roomId")
    ChatRoomStatistics findRoomStatistics(@Param("roomId") Long roomId);

    @Query("SELECT u.lastReadMessageSequence AS lastReadMessageSequence, COUNT(u) AS count " +
        "FROM UserChatRoom u " +
        "WHERE u.chatRoom.id = :roomId AND u.isConnected = false " +
        "GROUP BY u.lastReadMessageSequence")
    List<LastReadMessageSequenceGroup> findLastReadSequenceGroups(@Param("roomId") Long roomId);

    @Query("SELECT u.user.id AS userId "
        + "FROM UserChatRoom u "
        + "WHERE u.chatRoom.id =:roomId")
    List<ChatRoomParticipantGroups> findParticipantsByChatRoomId(@Param("roomId") Long roomId);
}
