package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.entity.ChatRoom;
import com.hymin.webtoon_review.chat.repository.projection.ChatRoomGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByWebtoonId(Long webtoonId);

    @Query("SELECT cr.id AS roomId, cr.name AS roomName, ucr.roomMemberId AS roomMemberId, "
        + "cr.lastMessage AS lastMessage, cr.lastMessageCreatedAt AS lastMessageCreatedAt "
        + "FROM ChatRoom cr "
        + "JOIN UserChatRoom ucr "
        + "ON cr.id = ucr.chatRoom.id "
        + "WHERE ucr.user.id =:userId")
    List<ChatRoomGroup> findChatRoomGroups(@Param("userId") Long userId);
}
