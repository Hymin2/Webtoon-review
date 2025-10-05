package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.RetryMessage;
import java.util.List;

public interface ChatMessageCustomRepository {

    void saveChatMessages(List<ChatMessage> chatMessages);

    void saveUnacknowledgedMessages(List<RetryMessage> RetryMessage);
}
