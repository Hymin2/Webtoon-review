package com.hymin.webtoon_review.chat.scheduler;

import com.hymin.webtoon_review.global.constant.RedisKeys;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("chat-worker")
@RequiredArgsConstructor
public class ChatWorkerHeartbeatScheduler {

    @Value("${server.instance.name:default}")
    private String serverName;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedRate = 10000)
    public void heartbeat() {
        long now = Instant.now().toEpochMilli();

        redisTemplate.opsForZSet().add(RedisKeys.CHAT_WORKER_HEALTH, serverName, now);
    }
}
