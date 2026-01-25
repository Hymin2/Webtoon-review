package com.hymin.webtoon_review.chat.controller;

import com.hymin.webtoon_review.chat.facade.ChatFacade;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatFacade chatFacade;

    @GetMapping("/chat/room")
    public RestResponse getRoomList(
        @Auth Authentication authentication
    ) {
        return ApiResponse.onSuccess(
            chatFacade.getRoomList((Long) authentication.getDetails())
        );
    }

    @GetMapping("/chat/room/{roomId}")
    public RestResponse getRoomInfo(
        @PathVariable("roomId") Long roomId,
        @Auth Authentication authentication
    ) {
        return ApiResponse.onSuccess(
            chatFacade.getRoomInfo(roomId, authentication.getName())
        );
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
