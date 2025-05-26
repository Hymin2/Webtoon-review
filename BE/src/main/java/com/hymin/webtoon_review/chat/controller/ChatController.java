package com.hymin.webtoon_review.chat.controller;

import com.hymin.webtoon_review.chat.dto.ChatRequest.ChatMessage;
import com.hymin.webtoon_review.chat.facade.ChatFacade;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacade chatFacade;

    @MessageMapping(value = "/chat/massages")
    public void sendMessage(
        @RequestBody ChatMessage chatMessage,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        chatMessage.setSender((String) headerAccessor.getSessionAttributes().get("nickname"));
        chatFacade.sendMessage(chatMessage);
    }

    @MessageMapping(value = "/chat/messages/connect")
    public void sendConnectMessage(@RequestBody ChatMessage chatMessage) {
        chatFacade.sendConnectDisconnectMessage(chatMessage);
    }

    @MessageMapping(value = "/chat/messages/disconnect")
    public void sendDisconnectMessage(@RequestBody ChatMessage chatMessage) {
        chatFacade.sendConnectDisconnectMessage(chatMessage);
    }

    @GetMapping("/chat/room/info")
    public RestResponse getRoomInfoPrevConnect(
        @RequestParam("webtoonId") Long webtoonId,
        @Auth Authentication authentication
    ) {
        return ApiResponse.onSuccess(
            chatFacade.getRoomInfoPrevConnect(webtoonId, authentication.getName()));
    }

    @PostMapping("/chat/room")
    public RestResponse joinChatRoom(
        @RequestParam("webtoonId") Long webtoonId,
        @Auth Authentication authentication
    ) {
        chatFacade.joinChatRoom(webtoonId, authentication.getName());

        return RestResponse.onCreated();
    }
}
