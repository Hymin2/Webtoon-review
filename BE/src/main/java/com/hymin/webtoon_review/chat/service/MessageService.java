package com.hymin.webtoon_review.chat.service;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final SimpMessageSendingOperations simpleMessageSendingOperations;

    public void send(String destination, ChatMessage chatMessage) {
        simpleMessageSendingOperations.convertAndSend(destination, chatMessage);
    }

    public void send(String username, String destination, ChatMessage chatMessage) {
        simpleMessageSendingOperations.convertAndSendToUser(username, destination, chatMessage);
    }
}
