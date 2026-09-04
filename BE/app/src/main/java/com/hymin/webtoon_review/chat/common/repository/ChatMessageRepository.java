package com.hymin.webtoon_review.chat.common.repository;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Optional<ChatMessage> findFirstByRoomIdOrderByMessageSequenceDesc(Long roomId);

    List<ChatMessage> findByRoomIdAndMessageSequenceGreaterThanOrderByMessageSequenceAsc(
        Long roomId,
        Long messageSequence
    );
}
