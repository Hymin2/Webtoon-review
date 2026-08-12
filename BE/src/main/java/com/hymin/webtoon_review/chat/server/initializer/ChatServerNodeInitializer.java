package com.hymin.webtoon_review.chat.server.initializer;

import com.hymin.webtoon_review.chat.server.listener.ChatMessageListener;
import com.hymin.webtoon_review.chat.server.listener.ChatWorkerEvenetMessageListener;
import com.hymin.webtoon_review.chat.server.route.ChatWorkerLocalHashRing;
import com.hymin.webtoon_review.chat.service.ChatSessionService;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import com.hymin.webtoon_review.global.constant.RedisTopicNames;
import com.hymin.webtoon_review.global.manager.MessageListenerManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat")
@RequiredArgsConstructor
public class ChatServerNodeInitializer {

    @Value("${server.instance.name:default}")
    private String serverName;

    private final ChatSessionService chatSessionService;
    private final ChatMessageListener chatMessageListener;
    private final ChatWorkerEvenetMessageListener chatWorkerEvenetMessageListener;

    private final RedisTemplate<String, String> redisTemplate;
    private final MessageListenerManager messageListenerManager;
    private final ChatWorkerLocalHashRing chatWorkerLocalHashRing;

    private final Duration ttl = Duration.ofSeconds(30L);

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("[채팅 서버 초기화 1/4] 서버 노드 등록");
        registerNode();

        log.info("[채팅 서버 초기화 2/4] ChatWorkerLocalHashRing 초기화");
        initLocalHashRing();

        log.info("[채팅 서버 초기화 3/4] Redis pub/sub 구독 시작");
        subscribeRedisPubSub();

        log.info("[채팅 서버 초기화 4/4] 채팅 서버 노드 생성 전파");
        broadcastJoinedServerNode();
    }

    @EventListener(ContextClosedEvent.class)
    public void destroy() {
        log.info("[채팅 서버 종료 1/4] 서버 노드 등록 해제");
        unregisterNode();

        log.info("[채팅 서버 종료 2/4] Redis pub/sub 구독 시작");
        unsubscribeRedisPubSub();

        log.info("[채팅 서버 종료 3/4] 사용자 세션 데이터 삭제");
        removeSessionData();

        log.info("[채팅 서버 종료 4/4] 채팅 서버 노드 종료 전파");
        broadcastExitedServerNode();
    }

    private void registerNode() {
        redisTemplate.opsForValue()
            .set(RedisKeys.ACTIVE_CHAT_SERVER_PREFIX + serverName, "ACTIVE", ttl);
    }

    private void unregisterNode() {
        redisTemplate.delete(RedisKeys.ACTIVE_CHAT_SERVER_PREFIX + serverName);
    }

    private void initLocalHashRing() {
        chatWorkerLocalHashRing.refresh();
    }

    private void subscribeRedisPubSub() {
        messageListenerManager.addSubscription(RedisTopicNames.CHAT_MESSAGE_PREFIX + serverName
            + RedisTopicNames.CHAT_MESSAGE_POSTFIX, chatMessageListener);
        messageListenerManager.addSubscription(RedisTopicNames.CHAT_WORKER_EVENTS,
            chatWorkerEvenetMessageListener);
    }

    private void unsubscribeRedisPubSub() {
        messageListenerManager.removeSubscription(RedisTopicNames.CHAT_MESSAGE_PREFIX + serverName
            + RedisTopicNames.CHAT_MESSAGE_POSTFIX);
        messageListenerManager.removeSubscription(RedisTopicNames.CHAT_WORKER_EVENTS);
    }

    private void broadcastJoinedServerNode() {
        redisTemplate.convertAndSend(RedisTopicNames.CHAT_EVENTS, serverName);
    }

    private void broadcastExitedServerNode() {
        redisTemplate.convertAndSend(RedisTopicNames.CHAT_EVENTS, "Exited: " + serverName);
    }

    private void removeSessionData() {
        chatSessionService.cleanupUserChatSession(serverName);
    }
}
