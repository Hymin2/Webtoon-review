package com.hymin.webtoon_review.global.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

@Component
@Profile("chat-worker")
@RequiredArgsConstructor
public class StreamListenerManager {

    private final RedisStreamGroupManager redisStreamGroupManager;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    private final Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();

    public void registerListener(String streamKey, String groupName, String consumerName,
        StreamListener<String, MapRecord<String, String, String>> listener) {

        redisStreamGroupManager.createStreamAndGroup(streamKey, groupName);

        Subscription subscription = this.listenerContainer.receive(
            Consumer.from(groupName, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            listener
        );

        subscriptionMap.put(streamKey, subscription);
    }

    public void removeListener(String streamKey) {
        Subscription subscription = subscriptionMap.remove(streamKey);
        if (subscription != null) {
            this.listenerContainer.remove(subscription);
        }
    }

}
