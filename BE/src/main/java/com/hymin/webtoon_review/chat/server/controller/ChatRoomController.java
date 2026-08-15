package com.hymin.webtoon_review.chat.server.controller;

import com.hymin.webtoon_review.chat.server.facade.ChatFacade;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import com.hymin.webtoon_review.global.response.RestResponse;
import jakarta.validation.constraints.PositiveOrZero;
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

    @GetMapping("/chat/room/{roomId}/messages")
    public RestResponse getMessagesAfter(
        @PathVariable("roomId") Long roomId,
        @RequestParam("messageSequence") @PositiveOrZero Long messageSequence,
        @Auth Authentication authentication
    ) {
        return ApiResponse.onSuccess(
            chatFacade.getMessagesAfter(
                roomId,
                messageSequence,
                (Long) authentication.getDetails()
            )
        );
    }

    @PostMapping("/chat/room")
    public RestResponse joinChatRoom(
        @RequestParam("webtoonId") Long webtoonId,
        @Auth Authentication authentication
    ) {
        return ApiResponse.of(
            ResponseStatus.CREATED,
            chatFacade.joinChatRoom(webtoonId, authentication.getName())
        );
    }
}
