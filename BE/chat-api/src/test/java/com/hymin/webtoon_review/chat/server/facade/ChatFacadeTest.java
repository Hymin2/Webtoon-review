package com.hymin.webtoon_review.chat.server.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hymin.webtoon_review.chat.common.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import com.hymin.webtoon_review.chat.server.service.ChatMessageRoutingService;
import com.hymin.webtoon_review.chat.server.service.ChatMessageQueryService;
import com.hymin.webtoon_review.chat.server.service.ChatServerMessageIdService;
import com.hymin.webtoon_review.chat.server.service.ChatService;
import com.hymin.webtoon_review.global.manager.TraceContextManager;
import com.hymin.webtoon_review.global.manager.TraceContextManager.TraceScope;
import com.hymin.webtoon_review.user.service.UserService;
import com.hymin.webtoon_review.webtoon.service.WebtoonService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatFacadeTest {

    @Mock
    private Tracer tracer;
    @Mock
    private TraceContextManager traceManager;
    @Mock
    private UserService userService;
    @Mock
    private ChatService chatService;
    @Mock
    private ChatMessageRoutingService chatMessageRoutingService;
    @Mock
    private ChatMessageQueryService chatMessageQueryService;
    @Mock
    private ChatServerMessageIdService chatServerMessageIdService;
    @Mock
    private WebtoonService webtoonService;
    @Mock
    private Span span;
    @Mock
    private Tracer.SpanInScope spanInScope;
    @Mock
    private TraceContext traceContext;

    private ChatFacade chatFacade;

    @BeforeEach
    void setUp() {
        chatFacade = new ChatFacade(
            tracer,
            traceManager,
            userService,
            chatService,
            chatMessageRoutingService,
            chatMessageQueryService,
            chatServerMessageIdService,
            webtoonService
        );
    }

    @Test
    void generatesMessageIdAfterReceivingMessage() {
        ChatMessageRequest request = new ChatMessageRequest(
            1L,
            "client-message-id",
            List.<MessageBlock>of()
        );
        when(traceManager.startNewSpan("chat-message-receive"))
            .thenReturn(new TraceScope(span, spanInScope));
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("trace-id");
        when(chatService.getRoomMemberId(2L, 1L)).thenReturn("room-member-id");
        when(chatServerMessageIdService.getOrCreate(1L, 2L, "client-message-id"))
            .thenReturn("server-message-id");

        chatFacade.sendMessage(request, 2L, "nickname");

        ArgumentCaptor<ChatMessageDto> captor = ArgumentCaptor.forClass(ChatMessageDto.class);
        verify(chatMessageRoutingService).route(captor.capture());
        ChatMessageDto routedMessage = captor.getValue();

        assertThat(routedMessage.getMessageId()).isEqualTo("server-message-id");
        assertThat(routedMessage.getClientMessageId()).isEqualTo("client-message-id");
    }
}
