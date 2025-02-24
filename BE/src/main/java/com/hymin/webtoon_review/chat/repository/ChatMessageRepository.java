package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<Message, Long>,
    ChatMessageCustomRepository {

}
