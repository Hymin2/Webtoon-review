package com.hymin.webtoon_review.chat.server.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.hymin.webtoon_review.chat.common.dto.ChatMessageDto;
import com.hymin.webtoon_review.chat.common.dto.ChatRequest.ChatMessageRequest;
import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import com.hymin.webtoon_review.chat.common.exception.InvalidChatRoomAccessException;
import com.hymin.webtoon_review.chat.common.service.ChatService;
import com.hymin.webtoon_review.chat.server.service.*;
import com.hymin.webtoon_review.global.manager.TraceContextManager;
import com.hymin.webtoon_review.global.manager.TraceContextManager.TraceScope;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatFacadeTest {
    @Mock TraceContextManager traceManager;
    @Mock ChatService chatService;
    @Mock ChatMessageRoutingService routingService;
    @Mock ChatMessageQueryService queryService;
    @Mock ChatServerMessageIdService messageIdService;
    @Mock TraceScope scope;
    private ChatFacade facade;

    @BeforeEach void setUp() { facade = new ChatFacade(traceManager, chatService, routingService,
            queryService, messageIdService); }

    @Test void generatesMessageIdAfterReceivingMessage() {
        ChatMessageRequest request = new ChatMessageRequest(1L, "client-message-id",
                List.<MessageBlock>of());
        when(traceManager.startNewSpan("chat-message-receive")).thenReturn(scope);
        when(scope.span()).thenReturn(mock(io.micrometer.tracing.Span.class));
        when(scope.span().context()).thenReturn(mock(io.micrometer.tracing.TraceContext.class));
        when(scope.span().context().traceId()).thenReturn("trace-id");
        when(chatService.getRoomMemberId(2L, 1L)).thenReturn("room-member-id");
        when(messageIdService.getOrCreate(1L, 2L, "client-message-id")).thenReturn("server-message-id");
        facade.sendMessage(request, 2L, "nickname");
        ArgumentCaptor<ChatMessageDto> captor = ArgumentCaptor.forClass(ChatMessageDto.class);
        verify(routingService).route(captor.capture());
        assertThat(captor.getValue().getMessageId()).isEqualTo("server-message-id");
        assertThat(captor.getValue().getClientMessageId()).isEqualTo("client-message-id");
    }

    @Test void checksMembershipBeforeQueryingMessageHistory() {
        when(chatService.getRoomMemberId(2L, 1L)).thenReturn("room-member-id");
        when(queryService.getMessagesAfter(1L, 3L)).thenReturn(List.of());

        assertThat(facade.getMessagesAfter(1L, 3L, 2L)).isEmpty();

        InOrder inOrder = inOrder(chatService, queryService);
        inOrder.verify(chatService).getRoomMemberId(2L, 1L);
        inOrder.verify(queryService).getMessagesAfter(1L, 3L);
    }

    @Test void doesNotQueryMessageHistoryWhenMembershipIsRejected() {
        when(chatService.getRoomMemberId(2L, 1L))
                .thenThrow(new InvalidChatRoomAccessException());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> facade.getMessagesAfter(1L, 3L, 2L))
            .isInstanceOf(InvalidChatRoomAccessException.class);

        verifyNoInteractions(queryService);
    }
}
