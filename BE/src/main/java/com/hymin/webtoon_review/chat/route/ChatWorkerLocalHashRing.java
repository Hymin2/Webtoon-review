package com.hymin.webtoon_review.chat.route;

import com.google.common.hash.Hashing;
import com.hymin.webtoon_review.global.constant.RedisKeys;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatWorkerLocalHashRing {

    private TreeMap<Long, String> ring = new TreeMap<>();

    private final RedisTemplate<String, String> redisTemplate;

    public synchronized void refresh() {
        Set<TypedTuple<String>> nodes =
            redisTemplate.opsForZSet().rangeWithScores(RedisKeys.CHAT_WORKER_HASH_RING_ZSET, 0, -1);
        TreeMap<Long, String> newRing = new TreeMap<>();

        if (nodes != null) {
            for (ZSetOperations.TypedTuple<String> node : nodes) {
                newRing.put(node.getScore().longValue(), node.getValue());
            }
        }

        this.ring = newRing;
    }

    public String getTargetServerStreamKey(Long roomId) {
        if (ring.isEmpty()) {
            return null;
        }

        long hash = Hashing.murmur3_128().hashLong(roomId).asLong();
        Long key = ring.ceilingKey(hash);

        if (key == null) {
            key = ring.firstKey();
        }

        return ring.get(key).split("#")[0];
    }
}
