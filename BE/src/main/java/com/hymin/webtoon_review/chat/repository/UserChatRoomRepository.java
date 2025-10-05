package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.entity.UserChatRoom;
import com.hymin.webtoon_review.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    Boolean existsByChatRoomIdAndUserUsername(Long roomId, String username);

    List<UserChatRoom> findByChatRoomId(Long chatRoomId);

    @Query(value = "select u from UserChatRoom uc join fetch User u on uc.user = u where uc.chatRoom.id =:roomId and uc.isConnected = true")
    List<User> findConnectedUserByRoomId(@Param("roomId") Long roomId);

    UserChatRoom findByUserNicknameAndChatRoomId(String userNickname, Long chatRoomId);
}
