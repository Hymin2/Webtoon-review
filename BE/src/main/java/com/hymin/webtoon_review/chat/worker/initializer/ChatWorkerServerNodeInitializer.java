package com.hymin.webtoon_review.chat.worker.initializer;

import com.google.common.hash.Hashing;
import com.hymin.webtoon_review.chat.worker.listener.ChatWorkerStreamListener;
import com.hymin.webtoon_review.global.constant.RedisGroupNames;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import com.hymin.webtoon_review.global.constant.RedisStreamKeys;
import com.hymin.webtoon_review.global.constant.RedisTopicNames;
import com.hymin.webtoon_review.global.manager.StreamListenerManager;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("chat-worker")
@RequiredArgsConstructor
public class ChatWorkerServerNodeInitializer {

    @Value("${server.instance.name:default}")
    private String serverName;
    private final Set<TypedTuple<String>> vNodes = new HashSet<>();

    private final StreamListenerManager streamListenerManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatWorkerStreamListener chatWorkerStreamListener;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("[채팅 워커 서버 초기화 1/4] 서버 노드 등록");
        registerNode();

        log.info("[채팅 워커 서버 초기화 2/4] Stream 구독 시작");
        subscribeStream();

        log.info("[채팅 워커 서버 초기화 3/4] 해시 값 생성 및 Zset에 저장");
        generateHashKeyAndSave();

        log.info("[채팅 워커 서버 초기화 4/4] 채팅 워커 서버 노드 생성 전파");
        broadcastJoinedServerNode();
    }

    @EventListener(ContextClosedEvent.class)
    public void destroy() {
        log.info("[채팅 워커 서버 종료 1/3] 서버 노드 등록 해제");
        unregisterNode();

        log.info("[채팅 워커 서버 종료 2/3] Zset에 해시 값 삭제");
        removeHashKey();

        log.info("[채팅 워커 서버 종료 3/3] 채팅 워커 서버 노드 종료 전파");
        broadcastExitedServerNode();
    }

    private void registerNode() {
        long now = Instant.now().toEpochMilli();

        redisTemplate.opsForZSet().add(RedisKeys.CHAT_WORKER_HEALTH, serverName, now);
    }

    private void unregisterNode() {
        redisTemplate.opsForZSet().remove(RedisKeys.CHAT_WORKER_HEALTH, serverName);
    }

    private void subscribeStream() {
        String streamKey = RedisStreamKeys.CHAR_WORKER_STREAM_KEY_PREFIX + serverName;
        String groupName = RedisGroupNames.CHAT_WORKER_GROUP_NAME;
        String consumerName = serverName;

        streamListenerManager.registerListener(
            streamKey, groupName, consumerName, chatWorkerStreamListener
        );
    }

    private void generateHashKeyAndSave() {
        String streamKey = RedisStreamKeys.CHAR_WORKER_STREAM_KEY_PREFIX + serverName;

        for (int i = 1; i <= 100; i++) {
            String vNodeName = serverName + ":" + i;
            long hash = Hashing.murmur3_128()
                .hashString(vNodeName, StandardCharsets.UTF_8)
                .asLong();

            vNodes.add(new DefaultTypedTuple<>(streamKey + "#" + i, (double) hash));
        }

        redisTemplate.opsForZSet()
            .add(RedisKeys.CHAT_WORKER_HASH_RING_ZSET, vNodes);
    }

    private void removeHashKey() {
        redisTemplate.opsForZSet().remove(RedisKeys.CHAT_WORKER_HASH_RING_ZSET,
            vNodes.stream().map(TypedTuple::getValue).toArray());
        redisTemplate.delete(RedisKeys.CHAT_WORKER_PREFIX + serverName
            + RedisKeys.CHAT_WORKER_HASH_VALUES_POSTFIX);
    }

    private void broadcastJoinedServerNode() {
        redisTemplate.convertAndSend(RedisTopicNames.CHAT_WORKER_EVENTS, "Joined: " + serverName);
    }

    private void broadcastExitedServerNode() {
        redisTemplate.convertAndSend(RedisTopicNames.CHAT_WORKER_EVENTS, "Exited: " + serverName);
    }
}
