package com.hymin.webtoon_review.global.manager;

import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

@Component
@Profile("chat-worker")
@RequiredArgsConstructor
public class StreamListenerManager {

    private final RedisTemplate<String, String> redisTemplate;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    private final Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();

    public void registerListener(String streamKey, String groupName, String consumerName,
        StreamListener<String, MapRecord<String, String, String>> listener) {

        createGroupAndStreamKey(streamKey, groupName);

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

    private void createGroupAndStreamKey(String streamKey, String groupName) {
        if (!redisTemplate.hasKey(streamKey)) {
            RedisAsyncCommands<String, String> commands = (RedisAsyncCommands<String, String>)
                this.redisTemplate.getConnectionFactory()
                    .getConnection()
                    .getNativeConnection();

            CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(CommandKeyword.CREATE)
                .add(streamKey)
                .add(groupName)
                .add("0")
                .add("MKSTREAM");

            commands.dispatch(CommandType.XGROUP, new StatusOutput(StringCodec.UTF8), args);
        } else {
            if (!isStreamConsumerGroupExist(streamKey, groupName)) {
                this.redisTemplate.opsForStream()
                    .createGroup(streamKey, ReadOffset.from("0"), groupName);
            }
        }
    }

    private boolean isStreamConsumerGroupExist(String streamKey, String groupName) {
        try {
            return redisTemplate.opsForStream().groups(streamKey).stream()
                .anyMatch(g -> g.groupName().equals(groupName));
        } catch (Exception e) {
            return false;
        }
    }
}
