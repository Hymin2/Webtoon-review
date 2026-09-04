package com.hymin.webtoon_review.chat.worker.service;

import com.hymin.webtoon_review.chat.worker.listener.ChatWorkerStreamListener;
import com.hymin.webtoon_review.global.constant.RedisGroupNames;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import com.hymin.webtoon_review.global.constant.RedisStreamKeys;
import com.hymin.webtoon_review.global.constant.RedisTopicNames;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("chat-worker")
@RequiredArgsConstructor
public class ChatWorkerRecoverService {

    @Value("${server.instance.name:default}")
    private String serverName;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatWorkerStreamListener chatWorkerStreamListener;

    @Async("chatWorkerRecoverExecutor")
    public void runRecovery() {
        String key = RedisKeys.CHAT_WORKER_HEALTH;

        List<TypedTuple<String>> serverList =
            new ArrayList<>(redisTemplate.opsForZSet().rangeWithScores(key, 0, -1));

        if (serverList.size() < 2) {
            return;
        }

        int myIdx = -1;
        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).getValue().equals(serverName)) {
                myIdx = i;
                break;
            }
        }

        if (myIdx == -1) {
            return;
        }
        int checkCount = Math.min(serverList.size() - 1, 3);
        long now = Instant.now().toEpochMilli();

        for (int i = 1; i <= checkCount; i++) {
            int targetIdx = (myIdx + i) % serverList.size();
            TypedTuple<String> target = serverList.get(targetIdx);

            String targetServerName = target.getValue();
            double lastHeartbeat = target.getScore();

            if (now - lastHeartbeat > 20000) {
                log.info("[채팅 워커 서버 Recover 1/8] 다른 채팅 워커 서버 죽음 감지");
                tryRecover(targetServerName);
            }
        }
    }

    private void tryRecover(String targetServerName) {
        String lockKey = RedisKeys.CHAT_WORKER_RECOVER_LOCK + targetServerName;

        if (acquireLock(lockKey, serverName, Duration.ofSeconds(30L))) {
            try {
                log.info("[채팅 워커 서버 Recover 2/8] Recover Lock 획득");
                removeHealth(targetServerName);
                log.info("[채팅 워커 서버 Recover 3/8] 채팅 워커 서버 Health 삭제");
                removeHashValues(targetServerName);
                log.info("[채팅 워커 서버 Recover 4/8] 해시 값 삭제");
                broadcastExitedServerNode(targetServerName);
                log.info("[채팅 워커 서버 Recover 5/8] 채팅 워커 서버 죽음 전파");
                processPendingMessages(targetServerName);
                log.info("[채팅 워커 서버 Recover 6/8] Pending Message 처리");
                processRemainingAllMessages(targetServerName);
                log.info("[채팅 워커 서버 Recover 7/8] 남아있는 모든 Message 처리");
            } finally {
                releaseLock(lockKey, serverName);
                log.info("[채팅 워커 서버 Recover 8/8] Recover Lock 해제");
            }
        }
    }

    private void removeHealth(String targetServerName) {
        redisTemplate.opsForZSet().remove(RedisKeys.CHAT_WORKER_HEALTH, targetServerName);
    }

    private void removeHashValues(String targetServerName) {
        String streamKey = RedisStreamKeys.CHAR_WORKER_STREAM_KEY_PREFIX + targetServerName;
        String[] vNodes = new String[100];

        for (int i = 1; i <= 100; i++) {
            vNodes[i - 1] = streamKey + "#" + i;
        }

        redisTemplate.opsForZSet().remove(RedisKeys.CHAT_WORKER_HASH_RING_ZSET, vNodes);
    }

    private void broadcastExitedServerNode(String targetServerName) {
        redisTemplate.convertAndSend(RedisTopicNames.CHAT_WORKER_EVENTS,
            "Down: " + targetServerName);
    }

    private void processPendingMessages(String targetServerName) {
        String targetStreamKey = RedisStreamKeys.CHAR_WORKER_STREAM_KEY_PREFIX + targetServerName;
        String groupName = RedisGroupNames.CHAT_WORKER_GROUP_NAME;
        String consumerName = serverName;

        while (true) {
            PendingMessages pendingMessages = redisTemplate.opsForStream()
                .pending(targetStreamKey, groupName, Range.unbounded(), 100L);

            if (pendingMessages.isEmpty()) {
                return;
            }

            RecordId[] recordIds = pendingMessages.stream()
                .map(PendingMessage::getId)
                .toArray(RecordId[]::new);

            StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
            List<MapRecord<String, String, String>> records = streamOps.claim(
                targetStreamKey,
                groupName,
                consumerName,
                Duration.ofSeconds(1L),
                recordIds
            );

            records.forEach(chatWorkerStreamListener::onMessage);
        }
    }

    private void processRemainingAllMessages(String targetServerName) {
        String streamKey = RedisStreamKeys.CHAR_WORKER_STREAM_KEY_PREFIX + targetServerName;
        String groupName = RedisGroupNames.CHAT_WORKER_GROUP_NAME;
        String consumerName = serverName;

        while (true) {
            StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
            List<MapRecord<String, String, String>> records = streamOps
                .read(Consumer.from(groupName, consumerName),
                    StreamReadOptions.empty().count(100),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()));

            if (records == null || records.isEmpty()) {
                break;
            }

            records.forEach(chatWorkerStreamListener::onMessage);
        }

        redisTemplate.delete(streamKey);
    }

    private boolean acquireLock(String lockKey, String lockValue, Duration timeout) {
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);

        return Boolean.TRUE.equals(result);
    }

    private void releaseLock(String lockKey, String lockValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end";

        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(lockKey), lockValue);
    }
}
