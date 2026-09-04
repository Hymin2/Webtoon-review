package com.hymin.webtoon_review.chat.server.controller;

import com.hymin.webtoon_review.chat.server.facade.ChatRoomFacade;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.response.RestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("chat")
@Validated
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomFacade facade;
    @GetMapping("/chat/room")
    public RestResponse getRoomList(@Auth Authentication auth) {
        return ApiResponse.onSuccess(facade.getRoomList((Long) auth.getDetails()));
    }
    @GetMapping("/chat/room/{roomId}")
    public RestResponse getRoomInfo(@PathVariable("roomId") Long roomId, @Auth Authentication auth) {
        return ApiResponse.onSuccess(facade.getRoomInfo(roomId, auth.getName()));
    }
    @PostMapping("/chat/room")
    public RestResponse joinChatRoom(@RequestParam("webtoonId") Long webtoonId,
            @Auth Authentication auth) {
        return ApiResponse.of(ResponseStatus.CREATED, facade.joinChatRoom(webtoonId, auth.getName()));
    }
}
