package com.hymin.webtoon_review.chat.controller;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.dto.ChatRequest.JoinExitMessage;
import com.hymin.webtoon_review.chat.facade.ChatFacade;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacade chatFacade;

    @MessageMapping(value = "/chat/massages")
    public void sendMessage(@RequestBody ChatMessage chatMessage,
        SimpMessageHeaderAccessor headerAccessor) {
        chatMessage.setSender((String) headerAccessor.getSessionAttributes().get("nickname"));

        chatFacade.sendMessage(chatMessage);
    }

    @MessageMapping(value = "/chat/messages/join-exit")
    public void joinExitMessage(@RequestBody JoinExitMessage joinExitMessage) {
        chatFacade.sendMessage(joinExitMessage);
    }

    @GetMapping("/chat/messages/{roomId}/counts")
    public RestResponse getReadCounts(@PathVariable("roomId") Long roomId,
        @Auth Authentication authentication) {
        return ApiResponse.onSuccess(chatFacade.getReadCount(roomId, authentication.getName()));
    }

    @PostMapping("/chat/room/{roomId}")
    public RestResponse joinChatRoom(@PathVariable("roomId") Long roomId,
        @Auth Authentication authentication) {
        chatFacade.joinChatRoom(roomId, authentication.getName());

        return RestResponse.onCreated();
    }
}
