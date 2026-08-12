package com.hymin.webtoon_review.global.inspector;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hymin.webtoon_review.chat.common.service.ChatSessionService;
import com.hymin.webtoon_review.global.security.JwtService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@ExtendWith(MockitoExtension.class)
class StompChannelInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private ChatSessionService chatSessionService;

    private StompChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new StompChannelInterceptor(jwtService, chatSessionService);
        ReflectionTestUtils.setField(interceptor, "serverName", "chat-1");
    }

    @Test
    void 구독하지_않은_세션도_사용자_연결_정보를_정리한다() {
        SessionDisconnectEvent event = createDisconnectEvent(Map.of(
            "userId", 1L,
            "clientId", "client-1"
        ));

        interceptor.handleDisconnect(event);

        verify(chatSessionService, never())
            .removeChatRoomOnlineMember(anyLong(), anyLong(), anyString());
        verify(chatSessionService).removeUserChatSession(1L, "client-1");
        verify(chatSessionService).removeServerConnectedUser(1L, "client-1", "chat-1");
    }

    @Test
    void 구독한_세션은_채팅방과_사용자_연결_정보를_모두_정리한다() {
        SessionDisconnectEvent event = createDisconnectEvent(Map.of(
            "userId", 1L,
            "clientId", "client-1",
            "subscriptionMap", Map.of("subscription-1", 10L, "subscription-2", 20L)
        ));

        interceptor.handleDisconnect(event);

        verify(chatSessionService).removeChatRoomOnlineMember(10L, 1L, "client-1");
        verify(chatSessionService).removeChatRoomOnlineMember(20L, 1L, "client-1");
        verify(chatSessionService).removeUserChatSession(1L, "client-1");
        verify(chatSessionService).removeServerConnectedUser(1L, "client-1", "chat-1");
    }

    @Test
    void CONNECT_완료_전_종료는_Redis_정리를_호출하지_않는다() {
        SessionDisconnectEvent event = createDisconnectEvent(null);

        interceptor.handleDisconnect(event);

        verifyNoInteractions(chatSessionService);
    }

    private SessionDisconnectEvent createDisconnectEvent(Map<String, Object> attributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId("session-1");
        accessor.setSessionAttributes(attributes);
        Message<byte[]> message = MessageBuilder.createMessage(
            new byte[0],
            accessor.getMessageHeaders()
        );

        return new SessionDisconnectEvent(this, message, "session-1", CloseStatus.NORMAL);
    }
}
