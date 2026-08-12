package com.hymin.webtoon_review.global.inspector;

import com.hymin.webtoon_review.chat.common.service.ChatSessionService;
import com.hymin.webtoon_review.global.security.JwtService;
import io.jsonwebtoken.Claims;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@Profile("chat")
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    @Value("${server.instance.name:default}")
    private String serverName;

    private final JwtService jwtService;
    private final ChatSessionService chatSessionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        handleCommand(accessor);
        return message;
    }

    private void handleCommand(StompHeaderAccessor accessor) {
        StompCommand command = accessor.getCommand();

        switch (command) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case UNSUBSCRIBE -> handleUnsubscribe(accessor);
        }
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        log.info("[채팅] 연결 시도");

        String accessToken = accessor.getFirstNativeHeader("Authorization");
        String clientId = accessor.getFirstNativeHeader("X-Client-Id");

        Claims claims = jwtService.parseJwt(accessToken);
        Long userId = claims.get("id", Long.class);
        Map<String, Object> attributes = accessor.getSessionAttributes();

        Principal principal = () -> userId + "_" + clientId;
        accessor.setUser(principal);

        if (attributes != null) {
            attributes.put("userId", userId);
            attributes.put("username", claims.get("username"));
            attributes.put("nickname", claims.get("nickname"));
            attributes.put("clientId", clientId);

            chatSessionService.addUserChatSession(userId, clientId, serverName);
            chatSessionService.addServerConnectedUser(userId, clientId, serverName);
        }

        log.info("[채팅] 연결 완료: userId={}", claims.get("id"));
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.contains("/room")) {
            return;
        }

        log.info("[채팅] 구독 시도: {}", destination);

        Long roomId = parseRoomId(destination);
        Long userId = getSessionAttribute(accessor, "userId", Long.class);
        String clientId = getSessionAttribute(accessor, "clientId", String.class);
        String subId = accessor.getSubscriptionId();

        updateSubscriptionMap(accessor, subId, roomId);
        chatSessionService.addChatRoomOnlineMember(roomId, userId, clientId);
        log.info("[채팅] {}번 방 구독 성공 (User: {})", roomId, userId);
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String subId = accessor.getSubscriptionId();
        Map<String, Long> subscriptionMap = getSubscriptionMap(accessor);

        if (subscriptionMap != null && subscriptionMap.containsKey(subId)) {
            Long roomId = subscriptionMap.remove(subId);
            Long userId = getSessionAttribute(accessor, "userId", Long.class);
            String clientId = getSessionAttribute(accessor, "clientId", String.class);

            chatSessionService.removeChatRoomOnlineMember(roomId, userId, clientId);
            removeSubscriptionMap(accessor, subId);
        }
    }

    @EventListener
    void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long userId = getSessionAttribute(accessor, "userId", Long.class);
        String clientId = getSessionAttribute(accessor, "clientId", String.class);
        Map<String, Long> subscriptionMap = getSubscriptionMap(accessor);
        log.info("[채팅] 연결 해제 시도: userId={}", userId);

        if (userId == null || clientId == null) {
            return;
        }

        if (subscriptionMap != null) {
            subscriptionMap.forEach((subId, roomId) ->
                chatSessionService.removeChatRoomOnlineMember(roomId, userId, clientId)
            );
        }

        chatSessionService.removeUserChatSession(userId, clientId);
        chatSessionService.removeServerConnectedUser(userId, clientId, serverName);
        log.info("[채팅] 연결 해제 성공: userId={}", userId);
    }

    private Long parseRoomId(String destination) {
        try {
            return Long.valueOf(destination.substring(destination.lastIndexOf("/") + 1));
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T getSessionAttribute(StompHeaderAccessor accessor, String key, Class<T> type) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        return (attributes != null) ? type.cast(attributes.get(key)) : null;
    }

    private Map<String, Long> getSubscriptionMap(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        return attributes == null
            ? null
            : (Map<String, Long>) attributes.get("subscriptionMap");
    }

    private void updateSubscriptionMap(StompHeaderAccessor accessor, String subId, Long roomId) {
        Map<String, Long> map = getSubscriptionMap(accessor);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            accessor.getSessionAttributes().put("subscriptionMap", map);
        }
        map.put(subId, roomId);
    }

    private void removeSubscriptionMap(StompHeaderAccessor accessor, String subId) {
        Map<String, Long> map = getSubscriptionMap(accessor);
        map.remove(subId);
    }
}
