package com.hymin.webtoon_review.chat.server.controller;

import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageCommand;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageResult;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageRequest;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageResponse;
import com.hymin.webtoon_review.chat.server.service.CreateChatMessageCommandService;
import com.hymin.webtoon_review.global.annotation.Auth;
import com.hymin.webtoon_review.global.response.ApiResponse;
import com.hymin.webtoon_review.global.response.ResponseStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("chat")
@Validated
@RequiredArgsConstructor
public class ChatMessageCommandController {

    private final CreateChatMessageCommandService commandService;

    @PostMapping("/chat/room/{roomId}/messages")
    public ResponseEntity<ApiResponse<CreateChatMessageResponse>> createMessage(
        @PathVariable("roomId") @Positive Long roomId,
        @Valid @RequestBody CreateChatMessageRequest request,
        @Auth Authentication authentication
    ) {
        CreateChatMessageResult result = commandService.create(new CreateChatMessageCommand(
            roomId,
            (Long) authentication.getDetails(),
            authentication.getName(),
            request.getClientMessageId(),
            request.getMessageBlocks()
        ));
        ResponseStatus responseStatus = result.created()
            ? ResponseStatus.CREATED
            : ResponseStatus.OK;
        HttpStatus httpStatus = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(ApiResponse.of(
            responseStatus,
            CreateChatMessageResponse.from(result.message())
        ));
    }
}
