package com.hymin.webtoon_review.chat.server.controller;

import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.server.facade.ChatFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacade chatFacade;

    @MessageMapping(value = "/chat/messages")
    public void sendMessage(
        @Valid @RequestBody ChatMessageRequest request,
        SimpMessageHeaderAccessor accessor
    ) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String nickname = (String) accessor.getSessionAttributes().get("nickname");

        chatFacade.sendMessage(request, userId, nickname);
    }
}
