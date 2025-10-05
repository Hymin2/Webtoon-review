package com.hymin.webtoon_review.chat.repository;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.RetryMessage;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class UnacknowledgedMessageRepository {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RetryMessage>> retryMessages = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, ConcurrentHashMap<String, RetryMessage>> getRetryMessages() {
        return retryMessages;
    }

    public void connectUser(String username) {
        retryMessages.putIfAbsent(username, new ConcurrentHashMap<>());
    }

    public void disconnectUser(String username) {
        retryMessages.remove(username);
    }

    public void addRetryMessage(String username, ChatMessage chatMessage) {
        retryMessages.get(username)
            .put(chatMessage.getMessageUUID(), RetryMessage.from(username, chatMessage));
    }

    public void removeRetryMessage(String username, String messageUUID) {
        retryMessages.get(username)
            .remove(messageUUID);
    }
}
