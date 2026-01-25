package com.hymin.webtoon_review.chat.controller;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.facade.ChatFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacade chatFacade;

    @MessageMapping(value = "/chat/messages")
    public void sendMessage(
        @RequestBody ChatMessageRequest request,
        SimpMessageHeaderAccessor accessor
    ) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String nickname = (String) accessor.getSessionAttributes().get("nickname");

        chatFacade.sendMessage(request, userId, nickname);
    }
}
