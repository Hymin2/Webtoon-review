package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import java.util.List;

public interface ChatMessageCustomRepository {

    void saveAll(List<ChatMessage> chatMessages);

}
