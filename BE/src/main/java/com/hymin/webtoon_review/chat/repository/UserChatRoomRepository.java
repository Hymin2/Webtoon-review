package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    Boolean existsByChatRoomIdAndUserUsername(Long roomId, String username);

    List<UserChatRoom> findByChatRoomId(Long chatRoomId);
}
