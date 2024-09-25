package com.hymin.gateway.redis;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisSortedSetService {

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private ReactiveZSetOperations<String, Object> zSetOperations;
    private static final String BASE_KEY = "ratelimit";

    @PostConstruct
    public void init() {
        zSetOperations = reactiveRedisOperations.opsForZSet();
    }

    public Mono<Boolean> add(String username, String request, double score) {
        String key = getKey(username, request);
        String value = UUID.randomUUID().toString();

        return zSetOperations.add(key, value, score);
    }

    public Flux<Object> get(String username, String request) {
        String key = getKey(username, request);

        return zSetOperations.range(key, Range.from(Bound.inclusive(0L)).to(Bound.inclusive(-1L)));
    }

    public Mono<Long> size(String username, String request) {
        String key = getKey(username, request);

        return zSetOperations.size(key);
    }

    public Mono<Boolean> setExpire(String username, String request, long second) {
        String key = getKey(username, request);

        return reactiveRedisOperations.expire(key, Duration.ofSeconds(second));
    }

    public Mono<Long> remove(String username, String request, String value) {
        String key = getKey(username, request);

        return zSetOperations.remove(key, value);
    }

    public Mono<Long> remove(String username, String request, Long from, Long to) {
        String key = getKey(username, request);

        return zSetOperations.removeRange(key,
            Range.from(Bound.inclusive(from)).to(Bound.inclusive(to)));
    }

    private String getKey(String username, String request) {
        return BASE_KEY + ":" + username + ":" + request;
    }
}
