package com.hymin.webtoon_review.chat.server.controller;

import com.hymin.webtoon_review.chat.server.facade.ChatFacade;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.RestResponse;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("chat")
@Validated
@RequiredArgsConstructor
public class ChatMessageHistoryController {
    private final ChatFacade chatFacade;

    @GetMapping("/chat/room/{roomId}/messages")
    public RestResponse getMessagesAfter(@PathVariable("roomId") Long roomId,
            @RequestParam("messageSequence") @PositiveOrZero Long messageSequence,
            @Auth Authentication authentication) {
        return ApiResponse.onSuccess(chatFacade.getMessagesAfter(roomId, messageSequence,
                (Long) authentication.getDetails()));
    }
}
