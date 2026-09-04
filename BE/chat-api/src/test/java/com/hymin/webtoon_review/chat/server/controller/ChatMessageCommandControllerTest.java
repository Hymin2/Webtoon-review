package com.hymin.webtoon_review.chat.server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageCommand;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageResult;
import com.hymin.webtoon_review.chat.server.service.CreateChatMessageCommandService;
import com.hymin.webtoon_review.global.handler.GlobalExceptionHandler;
import com.hymin.webtoon_review.global.resolver.AuthenticationResolver;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ChatMessageCommandControllerTest {

    private CreateChatMessageCommandService commandService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        commandService = mock(CreateChatMessageCommandService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ChatMessageCommandController(commandService)
            )
            .setCustomArgumentResolvers(new AuthenticationResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("sender-name", "");
        authentication.setDetails(7L);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mapsAuthenticatedRequestToCommandAndReturnsCreated() throws Exception {
        when(commandService.create(any())).thenReturn(new CreateChatMessageResult(
            true,
            message()
        ));

        mockMvc.perform(post("/chat/room/11/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientMessageId\":\"client-1\",\"messageBlocks\":[]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.messageId").value("message-1"))
            .andExpect(jsonPath("$.data.roomId").value(11))
            .andExpect(jsonPath("$.data.messageSequence").value(1))
            .andExpect(jsonPath("$.data.clientMessageId").value("client-1"));

        verify(commandService).create(new CreateChatMessageCommand(
            11L,
            7L,
            "sender-name",
            "client-1",
            List.of()
        ));
    }

    @Test
    void returnsOkForExistingDurableMessage() throws Exception {
        when(commandService.create(any())).thenReturn(new CreateChatMessageResult(
            false,
            message()
        ));

        mockMvc.perform(post("/chat/room/11/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientMessageId\":\"client-1\",\"messageBlocks\":[]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.messageId").value("message-1"));
    }

    @Test
    void rejectsInvalidRequestBeforeCommandExecution() throws Exception {
        mockMvc.perform(post("/chat/room/11/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientMessageId\":\"\",\"messageBlocks\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verify(commandService, never()).create(any());
    }

    private ChatMessage message() {
        return ChatMessage.builder()
            .id("message-1")
            .roomId(11L)
            .senderId(7L)
            .roomMemberId("member-7")
            .messageSequence(1L)
            .clientMessageId("client-1")
            .sender("sender-name")
            .createdAt("2026-09-02 04:00:00.123")
            .messageBlocks(List.of())
            .build();
    }

}
